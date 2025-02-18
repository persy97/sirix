/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.sirix.page;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.sirix.api.PageReadOnlyTrx;
import io.sirix.page.interfaces.Page;

import java.io.IOException;

/**
 * Persists pages on secondary storage.
 *
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class PagePersister {

  /**
   * Deserialize page.
   *
   * @param source source to read from
   * @param pageReadTrx instance of class, which implements the {@link PageReadOnlyTrx} interface
   * @return {@link Page} instance
   * @throws IOException if an exception during deserialization of a page occurs
   */
  public @NonNull Page deserializePage(final PageReadOnlyTrx pageReadTrx, final BytesIn<?> source,
      final SerializationType type) throws IOException {
    return PageKind.getKind(source.readByte()).deserializePage(pageReadTrx, source, type);
  }

  /**
   * Serialize page.
   *
   * @param sink output sink
   * @param page the {@link Page} to serialize
   * @throws IOException if an exception during serialization of a page occurs
   */
  public void serializePage(final PageReadOnlyTrx pageReadTrx, final BytesOut<?> sink, final Page page,
      final SerializationType type) throws IOException {
    PageKind.getKind(page.getClass()).serializePage(pageReadTrx, sink, page, type);
  }
}
