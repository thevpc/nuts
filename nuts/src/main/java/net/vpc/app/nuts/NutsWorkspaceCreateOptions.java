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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vpc on 1/23/17.
 */
public final class NutsWorkspaceCreateOptions implements Serializable, Cloneable {

    private boolean ignoreIfFound;
    private boolean createIfNotFound;
    private boolean saveIfCreated;
    private String archetype;
    private Set<String> excludedExtensions;
    private Set<String> excludedRepositories;

    public boolean isIgnoreIfFound() {
        return ignoreIfFound;
    }

    public NutsWorkspaceCreateOptions setIgnoreIfFound(boolean ignoreIfFound) {
        this.ignoreIfFound = ignoreIfFound;
        return this;
    }

    public boolean isCreateIfNotFound() {
        return createIfNotFound;
    }

    public NutsWorkspaceCreateOptions setCreateIfNotFound(boolean createIfNotFound) {
        this.createIfNotFound = createIfNotFound;
        return this;
    }

    public boolean isSaveIfCreated() {
        return saveIfCreated;
    }

    public NutsWorkspaceCreateOptions setSaveIfCreated(boolean saveIfCreated) {
        this.saveIfCreated = saveIfCreated;
        return this;
    }

    public String getArchetype() {
        return archetype;
    }

    public NutsWorkspaceCreateOptions setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    public NutsWorkspaceCreateOptions copy() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen",e);
        }
    }

    @Override
    protected NutsWorkspaceCreateOptions clone() throws CloneNotSupportedException {
        NutsWorkspaceCreateOptions clone = (NutsWorkspaceCreateOptions) super.clone();
        if (clone.excludedExtensions != null) {
            clone.excludedExtensions = new HashSet<>(clone.excludedExtensions);
        }
        if (clone.excludedRepositories != null) {
            clone.excludedRepositories = new HashSet<>(clone.excludedRepositories);
        }
        return clone;
    }

    public Set<String> getExcludedExtensions() {
        return excludedExtensions;
    }

    public NutsWorkspaceCreateOptions setExcludedExtensions(Set<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    public Set<String> getExcludedRepositories() {
        return excludedRepositories;
    }

    public NutsWorkspaceCreateOptions setExcludedRepositories(Set<String> excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }
}
