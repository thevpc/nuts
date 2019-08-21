/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.ndi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
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
            BufferedReader br = new BufferedReader(new InputStreamReader(LinuxNdi.class.getResource(resourcePath).openStream()));
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
                String replacement = sb.toString();
                if (replacement.startsWith("#") && replacement.contains("\n")) {
                    for (String s : replacement.split("\n")) {
                        if (!s.startsWith("#")) {
                            w.write("# ");
                        }
                        w.write(s);
                        w.write('\n');
                    }
                } else {
                    w.write(replacement);
                    w.write('\n');
                }
            }
            w.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String betterPath(String path1) {
        String home = System.getProperty("user.home");
        if(path1.startsWith(home+"/")) {
            return "~" + path1.substring(home.length());
        }
        return path1;
    }

    public static String longuestCommonParent(String path1, String path2) {
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
