package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.util.NCopiable;

import java.util.*;

public class InstallIdInfo implements NCopiable, Cloneable {

    public boolean extra;
    public String sid;
    public NId id;
    public boolean doRequire;
//    public boolean doRequireDependencies;
    public boolean ignored;
    public InstallFlags flags;
    public String doError;
//    public NInstallStatus oldInstallStatus;
    public Set<NId> forIds = new HashSet<>();
    public InstallIdCacheItem cacheItem;
    public boolean updateDefaultVersion;
    public List<NId> requiredForIds;
    public boolean resolveInstaller;
    public boolean loaded;
    public NDependencyScope scope;
    public NDefinition oldDef;

    public boolean isAlreadyRequired() {
        return cacheItem.getOldInstallStatus().isRequired();
    }

    public boolean isAlreadyInstalled() {
        return cacheItem.getOldInstallStatus().isInstalled();
    }

    public boolean isAlreadyExists() {
        return cacheItem.getOldInstallStatus().isInstalled()
                || cacheItem.getOldInstallStatus().isRequired();
    }

    @Override
    public InstallIdInfo copy() {
        return (InstallIdInfo) clone();
    }

    @Override
    protected InstallIdInfo clone() {
        try {
            InstallIdInfo c = (InstallIdInfo) super.clone();
            c.requiredForIds = requiredForIds == null ? null : new ArrayList<>(requiredForIds);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
