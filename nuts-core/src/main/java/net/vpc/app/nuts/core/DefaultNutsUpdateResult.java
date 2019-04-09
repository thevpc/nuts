/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsUpdateResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateResult implements NutsUpdateResult{

    private NutsId id;
    private NutsDefinition localVersion;
    private NutsDefinition availableVersion;
    private boolean updateForced;
    private boolean updateApplied;
    private boolean updateAvailable;

    @Override
    public NutsId getId() {
        return id;
    }

    public DefaultNutsUpdateResult setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsDefinition getLocalVersion() {
        return localVersion;
    }

    public DefaultNutsUpdateResult setLocalVersion(NutsDefinition localVersion) {
        this.localVersion = localVersion;
        return this;
    }

    @Override
    public NutsDefinition getAvailableVersion() {
        return availableVersion;
    }

    public DefaultNutsUpdateResult setAvailableVersion(NutsDefinition availableVersion) {
        this.availableVersion = availableVersion;
        return this;
    }

    @Override
    public boolean isUpdateForced() {
        return updateForced;
    }

    public DefaultNutsUpdateResult setUpdateForced(boolean updateForced) {
        this.updateForced = updateForced;
        return this;
    }

    @Override
    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public DefaultNutsUpdateResult setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
        return this;
    }

    @Override
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public DefaultNutsUpdateResult setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
        return this;
    }

}
