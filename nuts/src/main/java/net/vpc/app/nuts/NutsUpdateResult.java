/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public class NutsUpdateResult {

    private NutsId id;
    private NutsDefinition localVersion;
    private NutsDefinition availableVersion;
    private boolean updateForced;
    private boolean updateApplied;
    private boolean updateAvailable;

    public NutsId getId() {
        return id;
    }

    public NutsUpdateResult setId(NutsId id) {
        this.id = id;
        return this;
    }

    public NutsDefinition getLocalVersion() {
        return localVersion;
    }

    public NutsUpdateResult setLocalVersion(NutsDefinition localVersion) {
        this.localVersion = localVersion;
        return this;
    }

    public NutsDefinition getAvailableVersion() {
        return availableVersion;
    }

    public NutsUpdateResult setAvailableVersion(NutsDefinition availableVersion) {
        this.availableVersion = availableVersion;
        return this;
    }

    public boolean isUpdateForced() {
        return updateForced;
    }

    public NutsUpdateResult setUpdateForced(boolean updateForced) {
        this.updateForced = updateForced;
        return this;
    }

    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public NutsUpdateResult setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
        return this;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public NutsUpdateResult setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
        return this;
    }

}
