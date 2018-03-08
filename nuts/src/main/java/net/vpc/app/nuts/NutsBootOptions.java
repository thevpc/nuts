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
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NutsBootOptions implements Serializable {

    private String root;
    private String runtimeId;
    private String runtimeSourceURL;
    private NutsClassLoaderProvider classLoaderProvider;

    public String getRoot() {
        return root;
    }

    public NutsBootOptions setRoot(String workspaceRoot) {
        this.root = workspaceRoot;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootOptions setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeSourceURL() {
        return runtimeSourceURL;
    }

    public NutsBootOptions setRuntimeSourceURL(String runtimeSourceURL) {
        this.runtimeSourceURL = runtimeSourceURL;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsBootOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    @Override
    public String toString() {
        return "NutsBootOptions{" + "root=" + root + ", runtimeId=" + runtimeId + ", runtimeSourceURL=" + runtimeSourceURL + ", classLoaderProvider=" + classLoaderProvider + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.root);
        hash = 41 * hash + Objects.hashCode(this.runtimeId);
        hash = 41 * hash + Objects.hashCode(this.runtimeSourceURL);
        hash = 41 * hash + Objects.hashCode(this.classLoaderProvider);
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
        final NutsBootOptions other = (NutsBootOptions) obj;
        if (!Objects.equals(this.root, other.root)) {
            return false;
        }
        if (!Objects.equals(this.runtimeId, other.runtimeId)) {
            return false;
        }
        if (!Objects.equals(this.runtimeSourceURL, other.runtimeSourceURL)) {
            return false;
        }
        if (!Objects.equals(this.classLoaderProvider, other.classLoaderProvider)) {
            return false;
        }
        return true;
    }

}
