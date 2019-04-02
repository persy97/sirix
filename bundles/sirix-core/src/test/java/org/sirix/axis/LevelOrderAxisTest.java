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
package org.sirix.axis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sirix.Holder;
import org.sirix.XdmTestHelper;
import org.sirix.api.xdm.XdmNodeReadOnlyTrx;
import org.sirix.exception.SirixException;

/**
 * Test {@link LevelOrderAxis}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class LevelOrderAxisTest {

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
  public void testAxisConventions() throws SirixException {
    final XdmNodeReadOnlyTrx rtx = holder.getNodeReadTrx();

    rtx.moveTo(11L);
    AbsAxisTest.testIAxisConventions(new LevelOrderAxis.Builder(rtx).build(), new long[] {12L});
    rtx.moveTo(11L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(), new long[] {11L, 12L});
    rtx.moveTo(0L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(),
        new long[] {0L, 1L, 4L, 5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L});

    rtx.moveTo(4L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(),
        new long[] {4L, 5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L});

    rtx.moveTo(4L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).build(), new long[] {5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L});

    rtx.moveTo(6L);
    AbsAxisTest.testIAxisConventions(new LevelOrderAxis.Builder(rtx).build(), new long[] {7L});

    rtx.moveTo(6L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(), new long[] {6L, 7L});

    rtx.moveTo(2L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(), new long[] {});

    rtx.moveTo(3L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(), new long[] {});

    rtx.moveTo(2L);
    AbsAxisTest.testIAxisConventions(new LevelOrderAxis.Builder(rtx).build(), new long[] {});

    rtx.moveTo(3L);
    AbsAxisTest.testIAxisConventions(new LevelOrderAxis.Builder(rtx).build(), new long[] {});

    rtx.moveTo(6L);
    AbsAxisTest.testAxisConventionsNext(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(), new long[] {6L, 7L});

    rtx.moveTo(0L);
    AbsAxisTest.testAxisConventionsNext(
        new LevelOrderAxis.Builder(rtx).includeSelf().build(),
        new long[] {0L, 1L, 4L, 5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L});

    rtx.moveTo(0L);
    AbsAxisTest.testAxisConventionsNext(
        new LevelOrderAxis.Builder(rtx).includeSelf().includeNonStructuralNodes().build(),
        new long[] {0L, 1L, 2L, 3L, 4L, 5L, 8L, 9L, 13L, 6L, 7L, 10L, 11L, 12L});

    rtx.moveTo(0L);
    AbsAxisTest.testIAxisConventions(
        new LevelOrderAxis.Builder(rtx).includeSelf().includeNonStructuralNodes().build(),
        new long[] {0L, 1L, 2L, 3L, 4L, 5L, 8L, 9L, 13L, 6L, 7L, 10L, 11L, 12L});
  }

}
