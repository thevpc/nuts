/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Objects;

/**
 * simple dummy implementation of NutsId base functions
 * @author vpc
 */
final class NutsBootId {

    String groupId;
    String artifactId;
    String version;

    public NutsBootId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    static NutsBootId parse(String id) {
        int dots=id.indexOf(':');
        if(dots>0) {
            int dash = id.indexOf('#', dots + 1);
            if (dash < 0) {
                //maven will use a double ':' instead of #
                dash = id.indexOf(':', dots + 1);
            }
            if (dash >= 0) {
                return new NutsBootId(id.substring(0, dots), id.substring(dots+1,dash), id.substring(dash+1));
            }
            return new NutsBootId(id.substring(0, dots), id.substring(dots+1), "LATEST");
        }
        throw new NutsParseException("Unable to parse " + id);
    }

    @Override
    public String toString() {
        if (version == null) {
            return groupId + ":" + artifactId;
        }
        return groupId + ":" + artifactId + "#" + version;
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
        return true;
    }

}
