package org.sirix.rest

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.net.PemKeyCertOptions
import io.vertx.ext.auth.oauth2.OAuth2FlowType
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.impl.HttpStatusException
import io.vertx.kotlin.core.http.HttpServerOptions
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.auth.authenticateAwait
import io.vertx.kotlin.ext.auth.oauth2.OAuth2ClientOptions
import io.vertx.kotlin.ext.auth.oauth2.providers.KeycloakAuth
import kotlinx.coroutines.launch
import org.sirix.rest.crud.Create
import org.sirix.rest.crud.Delete
import org.sirix.rest.crud.Get
import org.sirix.rest.crud.Update
import java.nio.file.Paths


class SirixVerticle : CoroutineVerticle() {
    /** User home directory. */
    private val userHome = System.getProperty("user.home")

    /** Storage for databases: Sirix data in home directory. */
    private val location = Paths.get(userHome, "sirix-data")

    override suspend fun start() {
        val router = createRouter()

        // Start an HTTP/2 server
        val server = vertx.createHttpServer(HttpServerOptions()
                .setSsl(true)
                .setUseAlpn(true)
                .setPemKeyCertOptions(
                        PemKeyCertOptions().setKeyPath(location.resolve("key.pem").toString())
                                .setCertPath(
                                        location.resolve("cert.pem").toString())))

        server.requestHandler { router.handle(it) }
                .listenAwait(config.getInteger("https.port", 9443))
    }

    private suspend fun createRouter() = Router.router(vertx).apply {
        val keycloak = KeycloakAuth.discoverAwait(
                vertx,
                OAuth2ClientOptions()
                        .setFlow(OAuth2FlowType.PASSWORD)
                        .setSite("http://localhost:8080/auth/realms/master")
                        .setClientID("sirix")
                        .setClientSecret(config.getString("client.secret")))

        // To get the access token.
        post("/login").handler(BodyHandler.create()).coroutineHandler { rc ->
            val userJson = rc.bodyAsJson
            val user = keycloak.authenticateAwait(userJson)
            rc.response().end(user.principal().toString())
        }

        // Create.
        put("/:database").coroutineHandler {
            Auth(keycloak, "realm:create").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Create(location, false).handle(it)
        }

        put("/:database/:resource").coroutineHandler {
            Auth(keycloak, "realm:create").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Create(location, false).handle(it)
        }

        post("/:database").coroutineHandler {
            Auth(keycloak, "realm:create").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Create(location, true).handle(it)
        }

        // Update.
        post("/:database/:resource").coroutineHandler {
            Auth(keycloak, "realm:modify").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Update(location).handle(it)
        }

        // Get.
        get("/").coroutineHandler {
            Auth(keycloak, "realm:view").handle(it)
            it.next()
        }.coroutineHandler {
            Get(location).handle(it)
        }

        get("/:database/:resource").coroutineHandler {
            Auth(keycloak, "realm:view").handle(it)
            it.next()
        }.coroutineHandler {
            Get(location).handle(it)
        }

        get("/:database").coroutineHandler {
            Auth(keycloak, "realm:view").handle(it)
            it.next()
        }.coroutineHandler {
            Get(location).handle(it)
        }

        post("/").coroutineHandler {
            Auth(keycloak, "realm:view").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Get(location).handle(it)
        }

        post("/:database/:resource").coroutineHandler {
            Auth(keycloak, "realm:view").handle(it)
            it.next()
        }.handler(BodyHandler.create()).coroutineHandler {
            Get(location).handle(it)
        }

        // Delete.
        delete("/").coroutineHandler {
            Auth(keycloak, "realm:delete").handle(it)
            it.next()
        }.coroutineHandler {
            Delete(location).handle(it)
        }

        delete("/:database/:resource").coroutineHandler {
            Auth(keycloak, "realm:delete").handle(it)
            it.next()
        }.coroutineHandler {
            Delete(location).handle(it)
        }

        delete("/:database").coroutineHandler {
            Auth(keycloak, "realm:delete").handle(it)
            it.next()
        }.coroutineHandler {
            Delete(location).handle(it)
        }

        // Exception with status code
        route().handler { ctx ->
            ctx.fail(HttpStatusException(HttpResponseStatus.NOT_FOUND.code()))
        }

        route().failureHandler { failureRoutingContext ->
            val statusCode = failureRoutingContext.statusCode()
            val failure = failureRoutingContext.failure()

            if (statusCode == -1) {
                if (failure is HttpStatusException)
                    response(failureRoutingContext.response(), failure.statusCode, failure.message)
                else
                    response(failureRoutingContext.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                            failure.message)
            } else {
                response(failureRoutingContext.response(), statusCode, failure?.message)
            }
        }
    }

    private fun response(response: HttpServerResponse, statusCode: Int, failureMessage: String?) {
        response.setStatusCode(statusCode).end("Failure calling the RESTful API: $failureMessage")
    }

    /**
     * An extension method for simplifying coroutines usage with Vert.x Web routers.
     */
    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
        return handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }
}