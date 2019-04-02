package org.sirix.node.immutable.json;

import static com.google.common.base.Preconditions.checkNotNull;
import org.sirix.api.visitor.JsonNodeVisitor;
import org.sirix.api.visitor.VisitResult;
import org.sirix.node.Kind;
import org.sirix.node.interfaces.StructNode;
import org.sirix.node.json.NullNode;
import org.sirix.node.xdm.ElementNode;

/**
 * Immutable element wrapper.
 *
 * @author Johannes Lichtenberger
 *
 */
public final class ImmutableNullNode extends AbstractImmutableJsonStructuralNode {

  /** Mutable {@link NullNode}. */
  private final NullNode mNode;

  /**
   * Private constructor.
   *
   * @param node mutable {@link ElementNode}
   */
  private ImmutableNullNode(final NullNode node) {
    mNode = checkNotNull(node);
  }

  /**
   * Get an immutable JSON-array node instance.
   *
   * @param node the mutable {@link ImmutableNullNode} to wrap
   * @return immutable JSON-array node instance
   */
  public static ImmutableNullNode of(final NullNode node) {
    return new ImmutableNullNode(node);
  }

  @Override
  public VisitResult acceptVisitor(final JsonNodeVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public StructNode structDelegate() {
    return mNode;
  }

  @Override
  public Kind getKind() {
    return Kind.JSON_NULL_VALUE;
  }
}
