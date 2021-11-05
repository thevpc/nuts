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
package net.thevpc.nuts.runtime.standalone.parsers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.core.format.json.JsonStringBuffer;
import net.thevpc.nuts.spi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class NshDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static NutsId NSH;
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("nsh", "sh", "bash"));
    private NutsWorkspace ws;
    @Override
    public NutsDescriptor parse(NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        try {
            return readNutDescriptorFromBashScriptFile(parserContext.getSession(), parserContext.getFullStream());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        if(NSH==null){
            NutsSession session = criteria.getSession();
            NSH= NutsId.of("nsh",session);
        }
        NutsDescriptorContentParserContext ctr=criteria.getConstraints(NutsDescriptorContentParserContext.class);
        if(ctr!=null) {
            String e = NutsUtilStrings.trim(ctr.getFileExtension());
            switch (e) {
                case "":
                case "sh":
                case "nsh":
                case "bash": {
                    return DEFAULT_SUPPORT;
                }
            }
        }
        return NO_SUPPORT;
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

    private static NutsDescriptor readNutDescriptorFromBashScriptFile(NutsSession session, InputStream file) throws IOException {
//        NutsWorkspace ws = session.getWorkspace();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file));
            String line = null;
            boolean firstLine = true;
            JsonStringBuffer comment = new JsonStringBuffer(session);
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
            switch (sheban){
                case "/bin/sh":
                case "/bin/nsh":
                case "/bin/nuts":
                case "/bin/bash":{
                    //accept
                    break;
                }
                default:{
                    return null;
                }
            }
            if (comment.toString().trim().isEmpty()) {
                return NutsDescriptorBuilder.of(session)
                        .setId(NutsId.of("temp:nsh#1.0",session))
                        .setPackaging("nsh")
                        .setExecutor(new DefaultNutsArtifactCall(NutsId.of("net.thevpc.nuts.toolbox:nsh",session)))
                        .build();
            }
            return NutsDescriptorParser.of(session).parse(comment.getValidString());
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
