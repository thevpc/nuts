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
package net.thevpc.nuts.runtime.core.commands.ws;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsUpdateResult;

/**
 * Created by vpc on 6/23/17.
 */
public final class DefaultNutsUpdateResult implements NutsUpdateResult {

    private NutsId id;
    private NutsDefinition local;
    private NutsDefinition available;
    private NutsId[] dependencies;
    private boolean runtime;
    private boolean updateApplied;
    private boolean updateForced;
    private boolean updateVersionAvailable;
    private boolean updateStatusAvailable;
    private boolean installed;

    public DefaultNutsUpdateResult() {
    }

    public DefaultNutsUpdateResult(NutsId id, NutsDefinition local, NutsDefinition available, NutsId[] dependencies, boolean runtime) {
        this.id = id;
        this.local = local;
        this.available = available;
        this.runtime = runtime;
        this.dependencies = dependencies == null ? new NutsId[0] : dependencies;
    }

    public DefaultNutsUpdateResult setInstalled(boolean installed) {
        this.installed = installed;
        return this;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isRuntime() {
        return runtime;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDefinition getLocal() {
        return local;
    }

    @Override
    public NutsDefinition getAvailable() {
        return available;
    }

    @Override
    public NutsId[] getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public void setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
    }

    @Override
    public boolean isUpdateForced() {
        return updateForced;
    }

    public void setUpdateForced(boolean updateForced) {
        this.updateForced = updateForced;
    }

    @Override
    public boolean isUpdatable() {
        return isUpdateVersionAvailable() || isUpdateStatusAvailable() || isUpdateForced();
    }

    @Override
    public boolean isUpdateVersionAvailable() {
        return updateVersionAvailable;
    }

    public void setUpdateVersionAvailable(boolean updateVersion) {
        this.updateVersionAvailable = updateVersion;
    }

    @Override
    public boolean isUpdateStatusAvailable() {
        return updateStatusAvailable;
    }

    public void setUpdateStatusAvailable(boolean updateStatus) {
        this.updateStatusAvailable = updateStatus;
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
