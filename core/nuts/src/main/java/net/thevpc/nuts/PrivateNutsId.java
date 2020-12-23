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
package net.thevpc.nuts;

import java.util.Objects;

/**
 * simple and dummy implementation of NutsId base functions
 *
 * @author thevpc
 * @since 0.5.4
 * @category Internal
 */
final class PrivateNutsId {

    private final String groupId;
    private final String artifactId;
    private final String version;

    public PrivateNutsId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    static PrivateNutsId parse(String id) {
        int dots = id.indexOf(':');
        if (dots > 0) {
            int dash = id.indexOf('#', dots + 1);
            if (dash < 0) {
                //maven will use a double ':' instead of #
                dash = id.indexOf(':', dots + 1);
            }
            if (dash >= 0) {
                return new PrivateNutsId(id.substring(0, dots), id.substring(dots + 1, dash), id.substring(dash + 1));
            }
            return new PrivateNutsId(id.substring(0, dots), id.substring(dots + 1), NutsConstants.Versions.LATEST);
        }
        int dash = id.indexOf('#', dots + 1);
        if (dash < 0) {
            //maven will use a double ':' instead of #
            dash = id.indexOf(':', dots + 1);
        }
        if (dash >= 0) {
            return new PrivateNutsId("", id.substring(0, dash), id.substring(dash + 1));
        }
        return new PrivateNutsId("", id, NutsConstants.Versions.LATEST);
    }

    @Override
    public String toString() {
        String s=artifactId;
        if(groupId!=null && groupId.length()>0){
            s=groupId+":"+s;
        }
        if (version != null && version.length()>0) {
            s=s+="#" + version;
        }
        return s;
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
        final PrivateNutsId other = (PrivateNutsId) obj;
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }

}
