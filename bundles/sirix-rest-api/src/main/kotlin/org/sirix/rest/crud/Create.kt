package org.sirix.rest.crud

import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sirix.access.Databases
import org.sirix.access.conf.DatabaseConfiguration
import org.sirix.access.conf.ResourceConfiguration
import org.sirix.api.Database
import org.sirix.api.xdm.XdmNodeTrx
import org.sirix.api.xdm.XdmResourceManager
import org.sirix.rest.Serialize
import org.sirix.service.xml.serialize.XmlSerializer
import org.sirix.service.xml.shredder.XmlShredder
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class Create(private val location: Path, private val createMultipleResources: Boolean = false) {
    suspend fun handle(ctx: RoutingContext): Route {
        val databaseName = ctx.pathParam("database")

        if (createMultipleResources) {
            createMultipleResources(databaseName, ctx)
            return ctx.currentRoute()
        }

        val resource = ctx.pathParam("resource")

        if (resource == null) {
            val dbFile = location.resolve(databaseName)
            val dispatcher = ctx.vertx().dispatcher()
            createDatabaseIfNotExists(dbFile, dispatcher, ctx.vertx().orCreateContext)
            return ctx.currentRoute()
        }

        val resToStore = ctx.bodyAsString

        if (databaseName == null || resToStore == null || resToStore.isBlank()) {
            ctx.fail(IllegalArgumentException("Database name and resource data to store not given."))
        }

        shredder(databaseName, resource, resToStore, ctx)

        return ctx.currentRoute()
    }

    private suspend fun createMultipleResources(databaseName: String?, ctx: RoutingContext) {
        val dbFile = location.resolve(databaseName)
        val context = ctx.vertx().orCreateContext
        val dispatcher = ctx.vertx().dispatcher()
        val dbConfig = createDatabaseIfNotExists(dbFile, dispatcher, context)

        val database = Databases.openXdmDatabase(dbFile)

        database.use {
            ctx.fileUploads().forEach { fileUpload ->
                val fileName = fileUpload.fileName()
                val resConfig = ResourceConfiguration.Builder(fileName, dbConfig).build()

                createOrRemoveAndCreateResource(database, resConfig, fileName, dispatcher)

                val manager = database.getResourceManager(fileName)

                manager.use {
                    val wtx = manager.beginNodeTrx()
                    val buffer = ctx.vertx().fileSystem().readFileAwait(fileUpload.uploadedFileName())
                    insertSubtreeAsFirstChild(wtx, buffer.toString(StandardCharsets.UTF_8), context)
                }
            }
        }
    }

    private suspend fun shredder(dbPathName: String, resPathName: String = dbPathName, resFileToStore: String,
                                 ctx: RoutingContext) {
        val dbFile = location.resolve(dbPathName)
        val context = ctx.vertx().orCreateContext
        val dispatcher = ctx.vertx().dispatcher()
        val dbConfig = createDatabaseIfNotExists(dbFile, dispatcher, context)

        insertResource(dbFile, resPathName, dbConfig, dispatcher, resFileToStore, context, ctx)
    }

    private suspend fun insertResource(dbFile: Path?, resPathName: String,
                                       dbConfig: DatabaseConfiguration,
                                       dispatcher: CoroutineDispatcher,
                                       resFileToStore: String,
                                       context: Context,
                                       ctx: RoutingContext) {
        val database = Databases.openXdmDatabase(dbFile)

        database.use {
            val resConfig = ResourceConfiguration.Builder(resPathName, dbConfig).build()

            createOrRemoveAndCreateResource(database, resConfig, resPathName, dispatcher)

            val manager = database.getResourceManager(resPathName)

            manager.use {
                val wtx = manager.beginNodeTrx()
                insertSubtreeAsFirstChild(wtx, resFileToStore, context)
                serialize(manager, context, ctx)
            }
        }
    }

    private suspend fun serialize(manager: XdmResourceManager, vertxContext: Context,
                                  routingCtx:
                                  RoutingContext) {
        vertxContext.executeBlockingAwait(Handler<Future<Nothing>> {
            val out = ByteArrayOutputStream()
            val serializerBuilder = XmlSerializer.XmlSerializerBuilder(manager, out)
            val serializer = serializerBuilder.emitIDs().emitRESTful().emitRESTSequence().prettyPrint().build()

            Serialize().serializeXml(serializer, out, routingCtx)

            it.complete(null)
        })
    }

    private suspend fun createDatabaseIfNotExists(dbFile: Path,
                                                  dispatcher: CoroutineDispatcher,
                                                  context: Context): DatabaseConfiguration {
        return withContext(dispatcher) {
            val dbExists = Files.exists(dbFile)

            if (!dbExists) {
                context.executeBlockingAwait(Handler<Future<Nothing>> {
                    Files.createDirectories(dbFile.parent)
                })
            }

            val dbConfig = DatabaseConfiguration(dbFile)

            if (!Databases.existsDatabase(dbFile)) {
                Databases.createXdmDatabase(dbConfig)
            }

            dbConfig
        }
    }

    private suspend fun createOrRemoveAndCreateResource(database: Database<XdmResourceManager>,
                                                        resConfig: ResourceConfiguration?,
                                                        resPathName: String, dispatcher: CoroutineDispatcher) {
        withContext(dispatcher) {
            if (!database.createResource(resConfig)) {
                database.removeResource(resPathName)
                database.createResource(resConfig)
            }
        }
    }

    private suspend fun insertSubtreeAsFirstChild(wtx: XdmNodeTrx, resFileToStore: String, context: Context) {
        context.executeBlockingAwait(Handler<Future<Nothing>> {
            wtx.use {
                wtx.insertSubtreeAsFirstChild(XmlShredder.createStringReader(resFileToStore))
            }

            it.complete(null)
        })
    }
}
