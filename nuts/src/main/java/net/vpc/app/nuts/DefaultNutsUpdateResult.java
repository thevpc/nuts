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

/**
 * Created by vpc on 6/23/17.
 */
public final class DefaultNutsUpdateResult implements NutsUpdateResult{

    private NutsId id;
    private NutsDefinition local;
    private NutsDefinition available;
    private NutsId[] dependencies;
    private boolean runtime;
    private boolean updateApplied;
    private boolean updateForced;
    private boolean updateAvailable;

    public DefaultNutsUpdateResult() {
    }

    
    public DefaultNutsUpdateResult(NutsId id, NutsDefinition local, NutsDefinition available, NutsId[] dependencies,boolean runtime) {
        this.id = id;
        this.local = local;
        this.available = available;
        this.runtime = runtime;
        this.dependencies = dependencies;
    }

    public boolean isRuntime() {
        return runtime;
    }

    public NutsId getId() {
        return id;
    }

    public NutsDefinition getLocal() {
        return local;
    }

    public NutsDefinition getAvailable() {
        return available;
    }

    public NutsId[] getDependencies() {
        return dependencies;
    }

    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public void setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
    }

    public boolean isUpdateForced() {
        return updateForced;
    }

    public void setUpdateForced(boolean updateForced) {
        this.updateForced = updateForced;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public void setLocal(NutsDefinition local) {
        this.local = local;
    }

    public void setAvailable(NutsDefinition available) {
        this.available = available;
    }

    public void setId(NutsId id) {
        this.id = id;
    }
    
    
    
}
