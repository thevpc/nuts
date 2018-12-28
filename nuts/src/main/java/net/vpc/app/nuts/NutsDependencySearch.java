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

import java.util.*;

public class NutsDependencySearch {
    private boolean includeMain = false;
    private boolean trackNotFound = false;
    private Set<NutsId> noFoundResult = new HashSet<>();

    public NutsDependencySearch() {
    }

    public boolean isIncludeMain() {
        return includeMain;
    }

    public NutsDependencySearch setIncludeMain(boolean includeMain) {
        this.includeMain = includeMain;
        return this;
    }

    public boolean isTrackNotFound() {
        return trackNotFound;
    }

    public NutsDependencySearch setTrackNotFound(boolean trackNotFound) {
        this.trackNotFound = trackNotFound;
        return this;
    }

    public Set<NutsId> getNoFoundResult() {
        return noFoundResult;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.includeMain ? 1 : 0);
        hash = 41 * hash + (this.trackNotFound ? 1 : 0);
        hash = 41 * hash + Objects.hashCode(this.noFoundResult);
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
        final NutsDependencySearch other = (NutsDependencySearch) obj;
        if (this.includeMain != other.includeMain) {
            return false;
        }
        if (this.trackNotFound != other.trackNotFound) {
            return false;
        }
        if (!Objects.equals(this.noFoundResult, other.noFoundResult)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsDependencySearch{includeMain=" + includeMain + ", trackNotFound=" + trackNotFound  + ", noFoundResult=" + noFoundResult + '}';
    }

}
