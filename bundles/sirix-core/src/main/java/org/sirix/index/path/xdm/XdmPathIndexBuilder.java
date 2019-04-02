package org.sirix.index.path.xdm;

import org.sirix.access.trx.node.xdm.AbstractXdmNodeVisitor;
import org.sirix.api.visitor.VisitResult;
import org.sirix.index.path.PathIndexBuilder;
import org.sirix.node.immutable.xdm.ImmutableAttributeNode;
import org.sirix.node.immutable.xdm.ImmutableElement;

public final class XdmPathIndexBuilder extends AbstractXdmNodeVisitor {

  private final PathIndexBuilder mPathIndexBuilder;

  public XdmPathIndexBuilder(final PathIndexBuilder pathIndexBuilderDelegate) {
    mPathIndexBuilder = pathIndexBuilderDelegate;
  }

  @Override
  public VisitResult visit(ImmutableElement node) {
    return mPathIndexBuilder.process(node, node.getPathNodeKey());
  }

  @Override
  public VisitResult visit(ImmutableAttributeNode node) {
    return mPathIndexBuilder.process(node, node.getPathNodeKey());
  }

}
