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
 * <br>
 *
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

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NSession;

/**
 * Created by vpc on 1/23/17.
 */
public class ItemStreamInfo {

    private Map<String, List<String>> headers = new HashMap<>();
    private InputStream content;
    private NSession session;

    public ItemStreamInfo(InputStream header, InputStream content, NSession session) throws IOException {
        this.content = content;
        this.session = session;
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
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid boundary"));
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
//        public int skip() throws IOException {
//            int y = ss.skip();
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
