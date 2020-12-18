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
package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util;

import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.AnyNixNdi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NdiUtils {

    public static boolean setExecutable(Path path) {
        if (Files.exists(path) && !Files.isExecutable(path)) {
            PosixFileAttributeView p = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (p != null) {
                try {
                    Set<PosixFilePermission> old = new HashSet<>(p.readAttributes().permissions());
                    old.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, old);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
        }
        return false;
    }

    public static String generateScriptAsString(String resourcePath, Function<String, String> mapper) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(b));
        generateScript(resourcePath, w, mapper);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return b.toString();
    }

    public static void generateScript(String resourcePath, BufferedWriter w, Function<String, String> mapper) {
        try {
            String lineSeparator = System.getProperty("line.separator");
            BufferedReader br = new BufferedReader(new InputStreamReader(AnyNixNdi.class.getResource(resourcePath).openStream()));
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
            throw new UncheckedIOException(ex);
        }
    }

    public static String betterPath(String path1) {
        String home = System.getProperty("user.home");
        if (path1.startsWith(home + "/") || path1.startsWith(home + "\\")) {
            return "~" + path1.substring(home.length());
        }
        return path1;
    }

    public static String replaceFilePrefixes(String path, Map<String,String> map) {
        for (Map.Entry<String, String> e : map.entrySet()) {
            String v = replaceFilePrefix(path, e.getKey(), e.getValue());
            if(!v.equals(path)){
                return v;
            }
        }
        return path;
    }

    public static String replaceFilePrefix(String path, String prefix,String replacement) {
        String path1=path;
        String fs = File.separator;
        if(!prefix.endsWith(fs)){
            prefix=prefix+fs;
        }
        if(!path1.endsWith(fs)){
            path1=prefix+fs;
        }
        if(path1.equals(prefix)){
            if(replacement==null){
                return "";
            }
            return replacement;
        }
        if(path.startsWith(prefix)){
            if(replacement==null || replacement.equals("")){
                return path1.substring(prefix.length());
            }
            return replacement+fs+path1.substring(prefix.length());
        }
        return path;
    }

    public static String longestCommonParent(String path1, String path2) {
        int latestSlash = -1;
        final int len = Math.min(path1.length(), path2.length());
        for (int i = 0; i < len; i++) {
            if (path1.charAt(i) != path2.charAt(i)) {
                break;
            } else if (path1.charAt(i) == '/') {
                latestSlash = i;
            }
        }
        if (latestSlash <= 0) {
            return "";
        }
        return path1.substring(0, latestSlash + 1);
    }
}
