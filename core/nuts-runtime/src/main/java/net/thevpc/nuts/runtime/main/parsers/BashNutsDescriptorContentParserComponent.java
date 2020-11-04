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
package net.thevpc.nuts.runtime.main.parsers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.config.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.config.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.format.json.JsonStringBuffer;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class BashNutsDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final NutsId BASH = CoreNutsUtils.parseNutsId("bash");
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("sh", "bash"));//, "war", "ear"

    @Override
    public NutsDescriptor parse(NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        try {
            return readNutDescriptorFromBashScriptFile(parserContext.getWorkspace(), parserContext.getFullStream());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    private static String removeBashComment(String str) {
        int x = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '#' || str.charAt(i) == ' ') {
                x = i + 1;
            } else {
                break;
            }
        }
        if (x < str.length()) {
            return str.substring(x);
        }
        return "";
    }

    private static NutsDescriptor readNutDescriptorFromBashScriptFile(NutsWorkspace ws, InputStream file) throws IOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file));
            String line = null;
            boolean firstLine = true;
            NutsId id = null;
            String name = null;
            List<NutsDependency> deps = new ArrayList<NutsDependency>();
            List<NutsId> arch = new ArrayList<NutsId>();
            JsonStringBuffer comment = new JsonStringBuffer(ws);
            String sheban = "";
            boolean start = false;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("#")) {
                        line = removeBashComment(line);
                        if (firstLine) {
                            firstLine = false;
                            //sheban
                            if (line.startsWith("!")) {
                                sheban = line.substring(1).trim();
                            } else {
                                break;
                            }
                        }
                        if (!start && line.matches("@nuts((\\s|\\{).*)?")) {
                            start = true;
                            String substring = line.substring(line.indexOf("@nuts") + "@nuts".length());
                            if (comment.append(substring)) {
                                break;
                            }

                        } else if (start) {
                            if (comment.append(line)) {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            if (comment.toString().trim().isEmpty()) {
                return new DefaultNutsDescriptorBuilder()
                        .id(CoreNutsUtils.parseNutsId("temp:sh#1.0"))
                        .executable()
                        //                        .setExt("sh")
                        .packaging("sh")
                        .executor(new DefaultNutsArtifactCall(BASH))
                        .build();
            }
            return ws.descriptor().parser().parse(comment.getValidString());
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
