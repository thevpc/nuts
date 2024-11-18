/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.lib.common.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNUpdateCmd extends NWorkspaceCmdBase<NUpdateCmd> implements NUpdateCmd {

    protected boolean enableInstall = true;
    protected boolean updateApi = false;
    protected boolean updateRuntime = false;
    protected boolean updateExtensions = false;
    protected boolean updateInstalled = false;
    protected boolean updateCompanions = false;
    protected boolean includeOptional = false;
    protected NVersion forceBootAPIVersion;
    protected Instant expireTime;
    protected List<String> args;
    protected final List<NDependencyScope> scopes = new ArrayList<>();
    protected final List<NId> lockedIds = new ArrayList<>();
    protected final List<NId> ids = new ArrayList<>();

    protected NWorkspaceUpdateResult result;
    protected NRepositoryFilter repositoryFilter;

    public AbstractNUpdateCmd(NWorkspace workspace) {
        super(workspace, "update");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public List<NId> getIds() {
        return CoreCollectionUtils.unmodifiableList(ids);
    }

    @Override
    public NUpdateCmd addId(String id) {
        NSession session=workspace.currentSession();
        return addId(id == null ? null : NId.of(id).get());
    }

    @Override
    public NUpdateCmd addId(NId id) {
        if (id == null) {
            NSession session=workspace.currentSession();
            throw new NNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NUpdateCmd addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUpdateCmd addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUpdateCmd removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NUpdateCmd removeId(String id) {
        NSession session=workspace.currentSession();
        return removeId(NId.of(id).get());
    }

    @Override
    public NUpdateCmd addScope(NDependencyScope scope) {
        if (scope != null) {
            scopes.add(scope);
        }
        return this;
    }

    @Override
    public NUpdateCmd addScopes(NDependencyScope... scopes) {
        if (scopes != null) {
            for (NDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public NUpdateCmd addScopes(Collection<NDependencyScope> scopes) {
        if (scopes != null) {
            for (NDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public boolean isOptional() {
        return includeOptional;
    }

    @Override
    public NUpdateCmd setOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NUpdateCmd addArg(String arg) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (arg == null) {
            throw new NullPointerException();
        }
        this.args.add(arg);
        return this;
    }

    @Override
    public NUpdateCmd clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NUpdateCmd addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NUpdateCmd addArgs(Collection<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    @Override
    public List<NId> getLockedIds() {
        return CoreCollectionUtils.unmodifiableList(lockedIds);
    }

    @Override
    public boolean isEnableInstall() {
        return enableInstall;
    }

    @Override
    public NUpdateCmd setEnableInstall(boolean enableInstall) {
        this.enableInstall = enableInstall;
        return this;
    }

    private boolean isUpdateNone() {
        return !updateApi && !updateRuntime && !updateExtensions && !updateInstalled && !updateCompanions
                && ids.isEmpty();
    }

    @Override
    public boolean isApi() {
        if (updateApi || isUpdateNone()) {
            return true;
        }

        for (NId id : ids) {
            if (id.getShortName().equals(NConstants.Ids.NUTS_API)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInstalled() {
        return updateInstalled;
    }

    @Override
    public boolean isCompanions() {
        if (isApi()) {
            return true;
        }
        return updateCompanions;
    }

    @Override
    public boolean isRuntime() {
        if (isApi()) {
            return true;
        }
        if (updateRuntime) {
            return true;
        }
        for (NId id : ids) {
            if (id.getShortName().equals(workspace.getRuntimeId().getShortName())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public NUpdateCmd setApi(boolean enableMajorUpdates) {
        this.updateApi = enableMajorUpdates;
        return this;
    }

    @Override
    public NUpdateCmd setCompanions(boolean updateCompanions) {
        this.updateCompanions = updateCompanions;
        return this;
    }

    @Override
    public boolean isExtensions() {
        if (isApi()) {
            return true;
        }
        return updateExtensions;
    }

    @Override
    public NUpdateCmd setExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public NVersion getApiVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NUpdateCmd setApiVersion(NVersion value) {
        this.forceBootAPIVersion = value;
        return this;
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NWorkspaceUpdateResult getResult() {
        NSession session=workspace.currentSession();
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NUnexpectedException();
        }
        return result;
    }

    @Override
    public NUpdateCmd run() {
        return update();
    }

    @Override
    public NUpdateCmd checkUpdates(boolean applyUpdates) {
        checkUpdates();
        if (applyUpdates) {
            update();
        }
        return this;
    }

    @Override
    public NUpdateCmd setRuntime(boolean updateRuntime) {
        this.updateRuntime = updateRuntime;
        return this;
    }

    @Override
    public NUpdateCmd setInstalled(boolean enable) {
        this.updateInstalled = enable;
        return this;
    }

//    @Override
//    public NutsUpdateCommand workspace() {
//        setApi(true);
//        setRuntime(true);
//        setExtensions(true);
//        setCompanions(true);
//        return this;
//    }

//    @Override
//    public NutsUpdateCommand companions() {
//        return companions(false);
//    }
//
//    @Override
//    public NutsUpdateCommand companions(boolean enable) {
//        return setCompanions(enable);
//    }

    @Override
    public NUpdateCmd setAll() {
        setApi(true);
        setRuntime(true);
        setExtensions(true);
        setCompanions(true);
        setInstalled(true);
        return this;
    }

    @Override
    public NUpdateCmd clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NUpdateCmd addLockedId(NId id) {
        if (id != null) {
            lockedIds.add(id);
        }
        return this;
    }

    @Override
    public NUpdateCmd addLockedId(String id) {
        NSession session=workspace.currentSession();
        if (!NBlankable.isBlank(id)) {
            lockedIds.add(NId.of(id).get());
        }
        return this;
    }

    @Override
    public NUpdateCmd addLockedIds(NId... ids) {
        if (ids != null) {
            for (NId id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NUpdateCmd addLockedIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NUpdateCmd clearLockedIds() {
        this.lockedIds.clear();
        return this;
    }

    @Override
    public NUpdateCmd clearScopes() {
        this.scopes.clear();
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session=workspace.currentSession();
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "-a":
            case "--all": {
                cmdLine.skip();
                if (enabled) {
                    this.setAll();
                }
                return true;
            }
//            case "-w":
//            case "--ws":
//            case "--workspace": {
//                cmdLine.skip();
//                if (enabled) {
//                    this.getWorkspace();
//                }
//                return true;
//            }
            case "-i":
            case "--installed": {
                cmdLine.withNextFlag((v, r) -> this.setInstalled(v));
                return true;
            }
            case "-r":
            case "--runtime": {
                cmdLine.withNextFlag((v, r) -> this.setRuntime(v));
                return true;
            }
            case "-A":
            case "--api": {
                cmdLine.withNextFlag((v, r) -> this.setApi(v));
                return true;
            }

            case "-e":
            case "--extensions": {
                cmdLine.withNextFlag((v, r) -> this.setExtensions(v));
                return true;
            }
            case "-c":
            case "--companions": {
                cmdLine.withNextFlag((v, r) -> this.setCompanions(v));
                return true;
            }
            case "-v":
            case "--api-version":
            case "--to-version": {
                cmdLine.withNextEntry((v, r) -> this.setApiVersion(NVersion.of(v).get()));
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                if (enabled) {
                    this.addArgs(cmdLine.toStringArray());
                }
                cmdLine.skipAll();
                return true;
            }
            case "-N":
            case "--expire": {
                a = cmdLine.next().get();
                if (enabled) {
                    if (a.getStringValue() != null) {
                        expireTime = Instant.parse(a.getStringValue().get());
                    } else {
                        expireTime = Instant.now();
                    }
                }
                return true;
            }

            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(a.asString().get());
                    return true;
                }
            }
        }
    }

    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NUpdateCmd setRepositoryFilter(NRepositoryFilter repositoryFilter) {
        this.repositoryFilter = repositoryFilter;
        return this;
    }
}
