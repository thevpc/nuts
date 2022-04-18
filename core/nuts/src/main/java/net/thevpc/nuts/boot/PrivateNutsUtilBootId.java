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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.List;

class PrivateNutsUtilBootId {
    static NutsId[] parseBootIdList(String s) {
        List<NutsId> boots = new ArrayList<>();
        StringBuilder q = null;
        boolean inBrackets = false;
        for (char c : s.toCharArray()) {
            if (q == null) {
                q = new StringBuilder();
                if (c == '[' || c == ']') {
                    inBrackets = true;
                    q.append(c);
                } else if (c == ',' || Character.isWhitespace(c)) {
                    //ignore
                } else {
                    q.append(c);
                }
            } else {
                if (c == ',' || c == ' ') {
                    if (inBrackets) {
                        q.append(c);
                    } else {
                        boots.add(NutsId.of(q.toString()).get());
                        q = null;
                        inBrackets = false;
                    }
                } else if (c == '[' || c == ']') {
                    if (inBrackets) {
                        inBrackets = false;
                        q.append(c);
                    } else {
                        inBrackets = true;
                        q.append(c);
                    }
                } else {
                    q.append(c);
                }
            }
        }
        if (q != null) {
            boots.add(NutsId.of(q.toString()).get());
        }
        return boots.toArray(new NutsId[0]);
    }

    static boolean isAcceptDependency(NutsDependency s, NutsBootOptions bOptions) {
        boolean bootOptionals = PrivateNutsUtilWorkspaceOptions.isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if(NutsBlankable.isBlank(o) || Boolean.parseBoolean(o)){
            if (!bootOptionals && !PrivateNutsUtilWorkspaceOptions.isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        List<String> oss = PrivateNutsUtilCollections.uniqueNonBlankList(s.getCondition().getOs());
        List<String> archs = PrivateNutsUtilCollections.uniqueNonBlankList(s.getCondition().getArch());
        if(oss.isEmpty()){
            oss.add("");
        }
        if(archs.isEmpty()){
            archs.add("");
        }
        if (!oss.isEmpty()) {
            NutsOsFamily eos = NutsOsFamily.getCurrent();
            boolean osOk = false;
            for (String e : oss) {
                NutsId ee = NutsId.of(e).get();
                if (ee.getShortName().equalsIgnoreCase(eos.id())) {
                    if (PrivateNutsUtilDescriptors.accept(ee.getVersion(),NutsVersion.of(System.getProperty("os.version")).get())) {
                        osOk = true;
                    }
                    break;
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!archs.isEmpty()) {
            String earch = System.getProperty("os.arch");
            if (earch != null) {
                boolean archOk = false;
                for (String e : archs) {
                    if (!e.isEmpty()) {
                        if (e.equalsIgnoreCase(earch)) {
                            archOk = true;
                            break;
                        }
                    }
                }
                return archOk;
            }
        }
        return true;
    }
}
