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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNArtifactCall;
import net.thevpc.nuts.runtime.standalone.format.json.JsonStringBuffer;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NStringUtils;

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
@NComponentScope(NScopeType.WORKSPACE)
public class NshDescriptorContentParserComponent implements NDescriptorContentParserComponent {

    public static NId NSH;
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("nsh", "sh", "bash"));
    private NWorkspace workspace;
    public NshDescriptorContentParserComponent(NWorkspace workspace) {
        if(NSH==null){
            NSH= NId.of("nsh").get();
        }
        this.workspace=workspace;
    }

    @Override
    public NDescriptor parse(NDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        try {
            return readNutDescriptorFromBashScriptFile(parserContext.getFullStream());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        NDescriptorContentParserContext ctr=criteria.getConstraints(NDescriptorContentParserContext.class);
        if(ctr!=null) {
            String e = NStringUtils.trim(ctr.getFileExtension());
            switch (e) {
                case "":
                case "sh":
                case "nsh":
                case "bash": {
                    return NConstants.Support.DEFAULT_SUPPORT;
                }
            }
        }
        return NConstants.Support.NO_SUPPORT;
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

    private static NDescriptor readNutDescriptorFromBashScriptFile(InputStream file) throws IOException {
//        NutsWorkspace ws = session.getWorkspace();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file));
            String line = null;
            boolean firstLine = true;
            JsonStringBuffer comment = new JsonStringBuffer();
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
                return new DefaultNDescriptorBuilder()
                        .setId(NId.of("temp:nsh#1.0").get())
                        .setPackaging("nsh")
                        .setExecutor(new DefaultNArtifactCall(NId.of("net.thevpc.nuts.toolbox:nsh").get()))
                        .build();
            }
            return NDescriptorParser.of().parse(comment.getValidString()).get();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
