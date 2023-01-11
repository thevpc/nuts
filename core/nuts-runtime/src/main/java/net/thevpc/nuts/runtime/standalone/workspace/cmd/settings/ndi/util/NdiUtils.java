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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.util;


import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.AnyNixNdi;

import java.io.*;
import java.net.URL;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NdiUtils {

    public static String generateScriptAsString(String resourcePath, NSession session, Function<String, String> mapper) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(b));
        generateScript(resourcePath, session, w, mapper);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return b.toString();
    }

    public static void generateScript(String resourcePath, NSession session, BufferedWriter w, Function<String, String> mapper) {
        try {
            String lineSeparator = System.getProperty("line.separator");
            URL resource = AnyNixNdi.class.getResource(resourcePath);
            if (resource == null) {
                throw new NIllegalArgumentException(session, NMsg.ofC("resource not found %s",resourcePath));
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()));
            String line = null;
            Pattern PATTERN = Pattern.compile("[$][$](?<name>([^$]+))[$][$]");
            while ((line = br.readLine()) != null) {
                Matcher matcher = PATTERN.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String name = matcher.group("name");
                    String x = mapper.apply(name);
                    if (x == null) {
                        x = "$$" + name + "$$";
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
                }
                matcher.appendTail(sb);
                BufferedReader br2 = new BufferedReader(new StringReader(sb.toString()));
                String line2 = null;
                while ((line2 = br2.readLine()) != null) {
                    w.write(line2);
                    w.write(lineSeparator);
                }
            }
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

}
