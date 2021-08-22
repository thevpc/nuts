/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * simple and dummy implementation of NutsId base functions.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class NutsBootId {

    private final String groupId;
    private final String artifactId;
    private final NutsBootVersion version;
    private final boolean optional;
    private final String os;
    private final String arch;

    public NutsBootId(String groupId, String artifactId, NutsBootVersion version, boolean optional, String os, String arch) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.optional = optional;
        this.os = os == null ? "" : os;
        this.arch = arch == null ? "" : arch;
    }

    public String getOs() {
        return os;
    }

    public String getArch() {
        return arch;
    }

    public boolean isOptional() {
        return optional;
    }

    private static String readUntil(Reader r, boolean include, IntPredicate stop) {
        try {
            StringBuilder sb = new StringBuilder();
            while (true) {
                r.mark(1);
                int x = r.read();
                if (x < 0) {
                    break;
                }
                if (include) {
                    sb.append((char) x);
                }
                if (stop.test(x)) {
                    r.reset();
                    break;
                }
                if (!include) {
                    sb.append((char) x);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static NutsBootId[] parseAll(String id) {
        List<NutsBootId> all = new ArrayList<>();
        if (id != null) {
            StringReader r = new StringReader(id);
            boolean loop = true;
            while (loop) {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    sb.append(readUntil(r, false, x -> (Character.isWhitespace(x) || x == '[' || x == ']') || x == ',' || x == ';'));
                    int n = 0;
                    try {
                        n = r.read();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (n < 0) {
                        loop = false;
                        break;
                    } else if (n == '[' || n == ']') {
                        sb.append((char) n);
                        sb.append(readUntil(r, true, x -> (x == '[' || x == ']')));
                    } else {
                        //space ',' or ';'
                        break;
                    }
                }
                String s = sb.toString();
                s=s.trim();
                if(!s.isEmpty()) {
                    all.add(parse(s));
                }
            }
        }
        return all.toArray(new NutsBootId[0]);
    }

    public static NutsBootId parse(String id) {
        int interro = id.indexOf('?');
        boolean optional = false;
        String os = "";
        String arch = "";
        Map<String, String> props = null;
        if (interro >= 0) {
            String propsString = id.substring(interro + 1);
            id = id.substring(0, interro);
            props = new PrivateNutsUtils.StringMapParser("=", "&").parseMap(propsString);
            for (Map.Entry<String, String> entry : props.entrySet()) {
                switch (entry.getKey()) {
                    case NutsConstants.IdProperties.OPTIONAL: {
                        optional = NutsUtilStrings.parseBoolean(entry.getValue(), true, false);
                        break;
                    }
                    case NutsConstants.IdProperties.OS: {
                        os = entry.getValue();
                        break;
                    }
                    case NutsConstants.IdProperties.ARCH: {
                        arch = entry.getValue();
                        break;
                    }
                }
            }
        }
        int brackets1 = id.indexOf('[');
        int brackets2 = id.indexOf(']');
        int bracketsStart = brackets2 < 0 ? brackets1 : brackets1 < 0 ? brackets2 : Math.min(brackets1, brackets2);
        if (bracketsStart >= 0) {
            brackets1 = id.indexOf('[', bracketsStart + 1);
            brackets2 = id.indexOf(']', bracketsStart + 1);
            int bracketsEnd = brackets2 < 0 ? brackets1 : brackets1 < 0 ? brackets2 : Math.min(brackets1, brackets2);
            String verStr = null;
            boolean startInclude = id.charAt(bracketsStart) == '[';
            boolean endInclude = true;
            if (bracketsEnd < 0) {
                //some error, suppose '['
                verStr = id.substring(bracketsStart + 1);
            } else {
                verStr = id.substring(bracketsStart + 1, bracketsEnd);
                endInclude = id.charAt(bracketsEnd) == ']';
            }
            int vir = verStr.indexOf(',');
            String verFrom;
            String verTo;
            if (vir < 0) {

            }
        }
        int dots = id.indexOf(':');
        if (dots > 0) {
            int dash = id.indexOf('#', dots + 1);
            if (dash < 0) {
                //maven will use a double ':' instead of #
                dash = id.indexOf(':', dots + 1);
            }
            if (dash >= 0) {
                return new NutsBootId(id.substring(0, dots), id.substring(dots + 1, dash),
                        NutsBootVersion.parse(id.substring(dash + 1)),
                        optional, os, arch
                );
            }
            return new NutsBootId(id.substring(0, dots), id.substring(dots + 1), NutsBootVersion.parse(""),
                    optional, os, arch);
        }
        int dash = id.indexOf('#', dots + 1);
        if (dash < 0) {
            //maven will use a double ':' instead of #
            dash = id.indexOf(':', dots + 1);
        }
        if (dash >= 0) {
            return new NutsBootId("", id.substring(0, dash), NutsBootVersion.parse(id.substring(dash + 1)),
                    optional, os, arch);
        }
        return new NutsBootId("", id, NutsBootVersion.parse(""),
                optional, os, arch);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (groupId != null && groupId.length() > 0) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (version != null && !version.isBlank()) {
            sb.append("#").append(version);
        }
        boolean inter = false;
        if (optional) {
            if (!inter) {
                sb.append("?");
                inter = true;
            } else {
                sb.append("&");
            }
            sb.append("optional=true");
        }
        if (os.length() > 0) {
            if (!inter) {
                sb.append("?");
                inter = true;
            } else {
                sb.append("&");
            }
            sb.append("os=").append(os);
        }
        if (arch.length() > 0) {
            if (!inter) {
                sb.append("?");
                inter = true;
            } else {
                sb.append("&");
            }
            sb.append("arch=").append(arch);
        }
        return sb.toString();
    }

    public NutsBootVersion getVersion() {
        return version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getShortName() {
        if(groupId.isEmpty()){
            return artifactId;
        }
        return groupId + ":" + artifactId;
    }

    public String getLongName() {
        StringBuilder sb=new StringBuilder();
        if(!groupId.isEmpty()){
            sb.append(groupId);
            sb.append(":");
        }
        sb.append(artifactId);
        if(!version.isBlank()){
            sb.append("#");
            sb.append(version);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.groupId);
        hash = 53 * hash + Objects.hashCode(this.artifactId);
        hash = 53 * hash + Objects.hashCode(this.version);
        hash = 53 * hash + Objects.hashCode(this.optional);
        hash = 53 * hash + Objects.hashCode(this.arch);
        hash = 53 * hash + Objects.hashCode(this.os);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsBootId other = (NutsBootId) obj;
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.optional, other.optional)) {
            return false;
        }
        if (!Objects.equals(this.os, other.os)) {
            return false;
        }
        if (!Objects.equals(this.arch, other.arch)) {
            return false;
        }
        return true;
    }

}
