/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nutsserver.util;

import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsUnsupportedOperationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

/**
 * Created by vpc on 1/23/17.
 */
public class MultipartStreamHelper implements Iterable<ItemStreamInfo> {

    private MultipartStream2 stream;

    public MultipartStreamHelper(InputStream input,
            String contentType) {
        stream = new MultipartStream2(
                input, resolveBoundaryFromContentType(contentType), MultipartStream2.DEFAULT_BUFSIZE,
                null
        );
    }

    private static byte[] resolveBoundaryFromContentType(String contentType) {
        //multipart/form-data; boundary=1597f5e92b6
        for (String s : contentType.split(";")) {
            s = s.trim();
            if (s.startsWith("boundary=")) {
                return s.substring("boundary=".length()).getBytes();
            }
        }
        throw new NutsIllegalArgumentException(null, "Invalid boundary");
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
                        }
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
                throw new NutsException(null, e);
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
            throw new NutsUnsupportedOperationException(null, "remove not supported");
        }
    }
}
