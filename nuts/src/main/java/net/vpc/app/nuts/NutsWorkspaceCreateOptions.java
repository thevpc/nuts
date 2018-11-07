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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by vpc on 1/23/17.
 */
public final class NutsWorkspaceCreateOptions implements Serializable, Cloneable {

    private String workspace = null;
    private boolean ignoreIfFound;
    private boolean createIfNotFound;
    private boolean saveIfCreated;
    private String archetype;
    private String[] excludedExtensions;
    private String[] excludedRepositories;
    private String login = null;
    private String password = null;
    private boolean noColors = false;
    private long creationTime;

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceCreateOptions setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

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

    public String[] getExcludedExtensions() {
        return excludedExtensions;
    }

    public NutsWorkspaceCreateOptions setExcludedExtensions(String[] excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    public String[] getExcludedRepositories() {
        return excludedRepositories;
    }

    public NutsWorkspaceCreateOptions setExcludedRepositories(String[] excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public NutsWorkspaceCreateOptions setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NutsWorkspaceCreateOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isNoColors() {
        return noColors;
    }

    public NutsWorkspaceCreateOptions setNoColors(boolean noColors) {
        this.noColors = noColors;
        return this;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceCreateOptions(" + "ignoreIfFound=" + ignoreIfFound + ", createIfNotFound=" + createIfNotFound + ", saveIfCreated=" + saveIfCreated + ", archetype=" + archetype + ", excludedExtensions=" + excludedExtensions + ", excludedRepositories=" + excludedRepositories + ')';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.ignoreIfFound ? 1 : 0);
        hash = 79 * hash + (this.createIfNotFound ? 1 : 0);
        hash = 79 * hash + (this.saveIfCreated ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.archetype);
        hash = 79 * hash + Objects.hashCode(this.excludedExtensions);
        hash = 79 * hash + Objects.hashCode(this.excludedRepositories);
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
        final NutsWorkspaceCreateOptions other = (NutsWorkspaceCreateOptions) obj;
        if (this.ignoreIfFound != other.ignoreIfFound) {
            return false;
        }
        if (this.createIfNotFound != other.createIfNotFound) {
            return false;
        }
        if (this.saveIfCreated != other.saveIfCreated) {
            return false;
        }
        if (!Objects.equals(this.archetype, other.archetype)) {
            return false;
        }
        if (!Objects.equals(this.excludedExtensions, other.excludedExtensions)) {
            return false;
        }
        if (!Objects.equals(this.excludedRepositories, other.excludedRepositories)) {
            return false;
        }
        return true;
    }

    public NutsWorkspaceCreateOptions copy() {
        try {
            NutsWorkspaceCreateOptions t = (NutsWorkspaceCreateOptions) clone();
            t.setExcludedExtensions(t.getExcludedExtensions() == null ? null : Arrays.copyOf(t.getExcludedExtensions(), t.getExcludedExtensions().length));
            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen", e);
        }
    }

    public long getCreationTime() {
        return creationTime;
    }

    public NutsWorkspaceCreateOptions setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}
