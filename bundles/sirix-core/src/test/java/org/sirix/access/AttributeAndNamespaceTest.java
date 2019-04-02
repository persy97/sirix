/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sirix.access;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sirix.Holder;
import org.sirix.XdmTestHelper;
import org.sirix.exception.SirixException;

public class AttributeAndNamespaceTest {

  private Holder holder;

  @Before
  public void setUp() throws SirixException {
    XdmTestHelper.deleteEverything();
    XdmTestHelper.createTestDocument();
    holder = Holder.generateRtx();
  }

  @After
  public void tearDown() throws SirixException {
    holder.close();
    XdmTestHelper.closeEverything();
  }

  @Test
  public void testAttribute() throws SirixException {
    holder.getNodeReadTrx().moveTo(1L);
    assertEquals(1, holder.getNodeReadTrx().getAttributeCount());
    holder.getNodeReadTrx().moveToAttribute(0);
    assertEquals("i", holder.getNodeReadTrx().getName().getLocalName());

    holder.getNodeReadTrx().moveTo(9L);
    assertEquals(1, holder.getNodeReadTrx().getAttributeCount());
    holder.getNodeReadTrx().moveToAttribute(0);
    assertEquals(
        "p:x", new StringBuilder(holder.getNodeReadTrx().getName().getPrefix()).append(
            ":").append(holder.getNodeReadTrx().getName().getLocalName()).toString());
    assertEquals("ns", holder.getNodeReadTrx().getName().getNamespaceURI());
  }

  @Test
  public void testNamespace() throws SirixException {
    holder.getNodeReadTrx().moveTo(1L);
    assertEquals(1, holder.getNodeReadTrx().getNamespaceCount());
    holder.getNodeReadTrx().moveToNamespace(0);
    assertEquals("p", holder.getNodeReadTrx().getName().getPrefix());
    assertEquals("ns", holder.getNodeReadTrx().getName().getNamespaceURI());
  }
}
