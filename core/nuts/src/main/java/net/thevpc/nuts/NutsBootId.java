/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
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

import java.util.Map;
import java.util.Objects;

/**
 * simple and dummy implementation of NutsId base functions
 *
 * @author thevpc
 * @since 0.5.4
 * @category Internal
 */
public final class NutsBootId {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final boolean optional;
    private final String os;
    private final String arch;

    public NutsBootId(String groupId, String artifactId, String version, boolean optional, String os, String arch) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version == null ? "" : version;
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
                        optional = PrivateNutsUtils.parseBoolean(entry.getValue(), true, false);
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
        int dots = id.indexOf(':');
        if (dots > 0) {
            int dash = id.indexOf('#', dots + 1);
            if (dash < 0) {
                //maven will use a double ':' instead of #
                dash = id.indexOf(':', dots + 1);
            }
            if (dash >= 0) {
                return new NutsBootId(id.substring(0, dots), id.substring(dots + 1, dash), id.substring(dash + 1),
                        optional, os, arch
                );
            }
            return new NutsBootId(id.substring(0, dots), id.substring(dots + 1), NutsConstants.Versions.LATEST,
                    optional, os, arch);
        }
        int dash = id.indexOf('#', dots + 1);
        if (dash < 0) {
            //maven will use a double ':' instead of #
            dash = id.indexOf(':', dots + 1);
        }
        if (dash >= 0) {
            return new NutsBootId("", id.substring(0, dash), id.substring(dash + 1),
                    optional, os, arch);
        }
        return new NutsBootId("", id, NutsConstants.Versions.LATEST,
                optional, os, arch);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (groupId != null && groupId.length() > 0) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (version != null && version.length() > 0) {
            sb.append("#").append(version);
        }
        boolean inter = false;
        if (optional) {
            if (!inter) {
                sb.append("?");
                inter=true;
            }else{
                sb.append("&");
            }
            sb.append("optional=true");
        }
        if (os.length()>0) {
            if (!inter) {
                sb.append("?");
                inter=true;
            }else{
                sb.append("&");
            }
            sb.append("os=").append(os);
        }
        if (arch.length()>0) {
            if (!inter) {
                sb.append("?");
                inter=true;
            }else{
                sb.append("&");
            }
            sb.append("arch=").append(arch);
        }
        return sb.toString();
    }

    public String getVersion() {
        return version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getShortName() {
        return groupId + ":" + artifactId;
    }

    public String getLongName() {
        return groupId + ":" + artifactId + "#" + version;
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
