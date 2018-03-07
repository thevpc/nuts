/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.NutsIllegalArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/23/17.
 */
public class ItemStreamInfo {

    private Map<String, List<String>> headers = new HashMap<>();
    private InputStream content;

    public ItemStreamInfo(InputStream header, InputStream content) throws IOException {
        this.content = content;
        BufferedReader r = new BufferedReader(new InputStreamReader(header));
        String line = null;
        while ((line = r.readLine()) != null) {
            if (line.trim().length() > 0) {
                int i = line.indexOf(':');
                if (i > 0) {
                    String k = line.substring(0, i).trim();
                    String v = line.substring(i + 1).trim();
                    List<String> strings = headers.get(k);
                    if (strings == null) {
                        strings = new ArrayList<>();
                        headers.put(k, strings);
                    }
                    strings.add(v);
                }
            }
        }
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public InputStream getContent() {
        return content;
    }

    public String resolveVarInHeader(String contentType, String var) {
        List<String> strings = getHeaders().get(contentType);
        return resolveVarInContentType(strings.get(0), var);
    }

    public String resolveVarInContentType(String contentType, String var) {
        //multipart/form-data; boundary=1597f5e92b6
        for (String s : contentType.split(";")) {
            s = s.trim();
            if (s.startsWith(var + "=")) {
                String substring = s.substring((var + "=").length());
                substring = substring.trim();
                if (substring.startsWith("\"")) {
                    substring = substring.substring(1, substring.length() - 1);
                }
                return substring;
            }
        }
        throw new NutsIllegalArgumentException("Invalid boundary");
    }

//    private static class ErrInputStream extends InputStream {
//
//        private final InputStream ss;
//        private final byte[] refBytes;
//        int index;
//
//        public ErrInputStream(InputStream ss, byte[] refBytes) {
//            this.ss = ss;
//            this.refBytes = refBytes;
//            index = 0;
//        }
//
//        @Override
//        public int read() throws IOException {
//            int y = ss.read();
//            if (y < 0) {
//                return y;
//            }
//            int expected = refBytes[index] & 0xff;
//            if (y != expected) {
//                System.out.printf("Error at %s\n", index);
////                throw new IOException("Error");
//            }
//            index++;
//            return y;
//        }
//    }
}
