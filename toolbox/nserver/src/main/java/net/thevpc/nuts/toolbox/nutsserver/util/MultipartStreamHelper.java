/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nutsserver.util;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

import java.io.*;
import java.util.Iterator;

/**
 * Created by vpc on 1/23/17.
 */
public class MultipartStreamHelper implements Iterable<ItemStreamInfo> {

    private MultipartStream2 stream;
    private NSession session;

    public MultipartStreamHelper(InputStream input,
                                 String contentType, NSession session) {
        this.session = session;
        stream = new MultipartStream2(
                input, resolveBoundaryFromContentType(contentType, session), MultipartStream2.DEFAULT_BUFSIZE,
                null, session
        );
    }

    private static byte[] resolveBoundaryFromContentType(String contentType, NSession session) {
        //multipart/form-data; boundary=1597f5e92b6
        for (String s : contentType.split(";")) {
            s = s.trim();
            if (s.startsWith("boundary=")) {
                return s.substring("boundary=".length()).getBytes();
            }
        }
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid boundary"));
    }

    public Iterator<ItemStreamInfo> iterator() {
        return new MultipartStream2Iterator();
    }

    private ItemStreamInfo newInputStreamSplitted() throws IOException {
        final InputStream itemInputStream = stream.newInputStream();
        ByteArrayOutputStream headers = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int count = itemInputStream.read(buffer, 0, buffer.length - 4);
            for (int i = 0; i < count; i++) {
                if (buffer[i] == stream.CR) {
                    if (i + 3 >= (buffer.length - 4)) {
                        //skip some more
                        int x = itemInputStream.read(buffer, count, buffer.length - 4);
                        count += x;
                    }
                    if (i + 3 < (buffer.length - 4) && buffer[i + 1] == stream.LF && buffer[i + 2] == stream.CR && buffer[i + 3] == stream.LF) {
                        //
                        headers.write(buffer, 0, i);
                        final byte[] start = new byte[count - (i + 4)];
                        System.arraycopy(buffer, i + 4, start, 0, start.length);
                        return new ItemStreamInfo(
                                new ByteArrayInputStream(headers.toByteArray()),
                                new InputStream() {
                                    int index = 0;

                                    @Override
                                    public int read() throws IOException {
                                        if (index < start.length) {
                                            index++;
                                            return (start[index - 1] & 0xff);
                                        }
                                        return itemInputStream.read();
                                    }
                                }, session
                        );
                    }
                }
            }
            headers.write(buffer, 0, count);
        }
    }

    private class MultipartStream2Iterator implements Iterator<ItemStreamInfo> {
        boolean first = true;
        boolean nextPart = false;

        @Override
        public boolean hasNext() {
            try {
                if (first) {
                    first = false;
                    nextPart = stream.skipPreamble();
                } else {
                    nextPart = stream.readBoundary();
                }
                return nextPart;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new NException(session, NMsg.ofPlain("parse multipart failed"),e);
            }
        }

        @Override
        public ItemStreamInfo next() {
            try {
                return newInputStreamSplitted();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}
