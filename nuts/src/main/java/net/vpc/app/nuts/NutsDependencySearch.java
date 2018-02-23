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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NutsDependencySearch {

    private List<String> ids = new ArrayList<>();
    private boolean includeMain = false;
    private boolean trackNotFound = false;
    private TypedObject dependencyFilter;
    private NutsDependencyScope scope = NutsDependencyScope.RUN;
    private Set<NutsId> noFoundResult = new HashSet<>();

    public NutsDependencySearch() {

    }

    public NutsDependencySearch(String... ids) {
        addIds(ids);
    }

    public NutsDependencySearch(NutsId... ids) {
        addIds(ids);
    }

    public NutsDependencySearch addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    public NutsDependencySearch addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    public NutsDependencySearch addId(String id) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
        return this;
    }

    public String[] getIds() {
        return this.ids.toArray(new String[this.ids.size()]);
    }

    public boolean isIncludeMain() {
        return includeMain;
    }

    public NutsDependencySearch setIncludeMain(boolean includeMain) {
        this.includeMain = includeMain;
        return this;
    }

    public NutsDependencySearch setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = new TypedObject(NutsDependencyFilter.class, filter, null);
        return this;
    }

    public NutsDependencyScope getScope() {
        return scope;
    }

    public NutsDependencySearch setScope(NutsDependencyScope scope) {
        this.scope = scope;
        return this;
    }

    public TypedObject getDependencyFilter() {
        return dependencyFilter;
    }

    public NutsDependencySearch setDependencyFilter(String filter) {
        this.dependencyFilter = new TypedObject(String.class, filter, null);
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
}
