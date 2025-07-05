/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NCoreCollectionUtils;
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
        super("update");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public List<NId> getIds() {
        return NCoreCollectionUtils.unmodifiableList(ids);
    }

    @Override
    public NUpdateCmd addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NUpdateCmd addId(NId id) {
        if (id == null) {
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
        return removeId(NId.get(id).get());
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
        return NCoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NUpdateCmd addArg(String arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
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
                if (arg != null) {
                    this.args.add(arg);
                }
            }
        }
        return this;
    }

    @Override
    public List<NId> getLockedIds() {
        return NCoreCollectionUtils.unmodifiableList(lockedIds);
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
            if (id.getShortName().equals(NWorkspace.of().getRuntimeId().getShortName())) {
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
        if (!NBlankable.isBlank(id)) {
            lockedIds.add(NId.get(id).get());
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
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isUncommented();
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
                return cmdLine.matcher().matchFlag((v) -> this.setInstalled(v.booleanValue())).anyMatch();
            }
            case "-r":
            case "--runtime": {
                return cmdLine.matcher().matchFlag((v) -> this.setRuntime(v.booleanValue())).anyMatch();
            }
            case "-A":
            case "--api": {
                return cmdLine.matcher().matchFlag((v) -> this.setApi(v.booleanValue())).anyMatch();
            }

            case "-e":
            case "--extensions": {
                return cmdLine.matcher().matchFlag((v) -> this.setExtensions(v.booleanValue())).anyMatch();
            }
            case "-c":
            case "--companions": {
                return cmdLine.matcher().matchFlag((v) -> this.setCompanions(v.booleanValue())).anyMatch();
            }
            case "-v":
            case "--api-version":
            case "--to-version": {
                return cmdLine.matcher().matchEntry((v) -> this.setApiVersion(NVersion.get(v.stringValue()).get())).anyMatch();
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
                    if (NBlankable.isNonBlank(a.getStringValue().orNull())) {
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
