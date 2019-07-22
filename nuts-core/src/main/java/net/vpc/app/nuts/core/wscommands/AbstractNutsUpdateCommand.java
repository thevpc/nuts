/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.util.*;

/**
 * type: Command Class
 *
 * @author vpc
 */
public abstract class AbstractNutsUpdateCommand extends NutsWorkspaceCommandBase<NutsUpdateCommand> implements NutsUpdateCommand {

    protected boolean enableInstall = true;
    protected boolean updateApi = false;
    protected boolean updateRuntime = false;
    protected boolean updateExtensions = false;
    protected boolean updateInstalled = false;
    protected boolean updateCompanions = false;
    protected boolean includeOptional = false;
    protected String forceBootAPIVersion;
    protected List<String> args;
    protected final List<NutsDependencyScope> scopes = new ArrayList<>();
    protected final List<NutsId> frozenIds = new ArrayList<>();
    protected final List<NutsId> ids = new ArrayList<>();

    protected NutsWorkspaceUpdateResult result;

    public AbstractNutsUpdateCommand(NutsWorkspace ws) {
        super(ws, "update");
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsUpdateCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsUpdateCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsUpdateCommand addId(String id) {
        return addId(id == null ? null : ws.id().parse(id));
    }

    @Override
    public NutsUpdateCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand ids(String... ids) {
        return addIds(ids);
    }

    @Override
    public NutsUpdateCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    @Override
    public NutsUpdateCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand removeId(String id) {
        return removeId(ws.id().parse(id));
    }

    @Override
    public NutsUpdateCommand scope(NutsDependencyScope scope) {
        return addScope(scope);
    }

    @Override
    public NutsUpdateCommand addScope(NutsDependencyScope scope) {
        if (scope != null) {
            scopes.add(scope);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand scopes(NutsDependencyScope... scopes) {
        return addScopes(scopes);
    }

    @Override
    public NutsUpdateCommand scopes(Collection<NutsDependencyScope> scopes) {
        return addScopes(scopes);
    }

    @Override
    public NutsUpdateCommand addScopes(NutsDependencyScope... scopes) {
        if (scopes != null) {
            for (NutsDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes) {
        if (scopes != null) {
            for (NutsDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public boolean isIncludeOptional() {
        return includeOptional;
    }

    @Override
    public NutsUpdateCommand setIncludeOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsUpdateCommand addArg(String arg) {
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
    public NutsUpdateCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsUpdateCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsUpdateCommand addArgs(Collection<String> args) {
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
    public NutsId[] getFrozenIds() {
        return frozenIds == null ? new NutsId[0] : frozenIds.toArray(new NutsId[0]);
    }

    @Override
    public boolean isEnableInstall() {
        return enableInstall;
    }

    @Override
    public NutsUpdateCommand enableInstall() {
        return enableInstall(true);
    }

    @Override
    public NutsUpdateCommand enableInstall(boolean enableInstall) {
        return setEnableInstall(enableInstall);
    }

    @Override
    public NutsUpdateCommand setEnableInstall(boolean enableInstall) {
        this.enableInstall = enableInstall;
        return this;
    }

    private boolean isUpdateNone() {
        return !updateApi && !updateRuntime && !updateExtensions && !updateInstalled && !updateCompanions
                && ids.isEmpty();
    }

    @Override
    public boolean isUpdateApi() {
        if (updateApi || isUpdateNone()) {
            return true;
        }

        for (NutsId id : ids) {
            if (id.getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUpdateInstalled() {
        return updateInstalled;
    }

    @Override
    public boolean isUpdateCompanions() {
        if (isUpdateApi()) {
            return true;
        }
        return updateCompanions;
    }

    @Override
    public boolean isUpdateRuntime() {
        if (isUpdateApi()) {
            return true;
        }
        if (updateRuntime) {
            return true;
        }
        for (NutsId id : ids) {
            if (id.getShortName().equals(ws.config().getRuntimeId().getShortName())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public NutsUpdateCommand setUpdateApi(boolean enableMajorUpdates) {
        this.updateApi = enableMajorUpdates;
        return this;
    }

    @Override
    public NutsUpdateCommand setUpdateCompanions(boolean updateCompanions) {
        this.updateCompanions = updateCompanions;
        return this;
    }

    @Override
    public boolean isUpdateExtensions() {
        if (isUpdateApi()) {
            return true;
        }
        return updateExtensions;
    }

    @Override
    public NutsUpdateCommand setUpdateExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public String getApiVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NutsUpdateCommand setApiVersion(String forceBootAPIVersion) {
        this.forceBootAPIVersion = forceBootAPIVersion;
        return this;
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NutsWorkspaceUpdateResult getResult() {
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NutsUnexpectedException(ws);
        }
        return result;
    }

    @Override
    public NutsUpdateCommand run() {
        return update();
    }

    @Override
    public NutsUpdateCommand checkUpdates(boolean applyUpdates) {
        checkUpdates();
        if (applyUpdates) {
            update();
        }
        return this;
    }

    @Override
    public NutsUpdateCommand setUpdateRuntime(boolean updateRuntime) {
        this.updateRuntime = updateRuntime;
        return this;
    }

    @Override
    public NutsUpdateCommand setUpdateInstalled(boolean updateInstalled) {
        this.updateInstalled = updateInstalled;
        return this;
    }

    @Override
    public NutsUpdateCommand workspace() {
        setUpdateApi(true);
        setUpdateRuntime(true);
        setUpdateExtensions(true);
        return this;
    }

    @Override
    public NutsUpdateCommand api() {
        return api(true);
    }

    @Override
    public NutsUpdateCommand api(boolean enable) {
        setUpdateApi(enable);
        return this;
    }

    @Override
    public NutsUpdateCommand runtime() {
        return runtime(true);
    }

    @Override
    public NutsUpdateCommand runtime(boolean enable) {
        return setUpdateRuntime(enable);
    }

    @Override
    public NutsUpdateCommand extensions() {
        return extensions(true);
    }

    @Override
    public NutsUpdateCommand companions(boolean enable) {
        return setUpdateCompanions(enable);
    }

    @Override
    public NutsUpdateCommand extensions(boolean enable) {
        return setUpdateExtensions(enable);
    }

    @Override
    public NutsUpdateCommand installed() {
        return installed(true);
    }

    @Override
    public NutsUpdateCommand installed(boolean enable) {
        return setUpdateInstalled(enable);
    }

    @Override
    public NutsUpdateCommand arg(String arg) {
        return addArg(arg);
    }

    @Override
    public NutsUpdateCommand args(String... arg) {
        return addArgs(arg);
    }

    @Override
    public NutsUpdateCommand args(Collection<String> arg) {
        return addArgs(arg);
    }

    @Override
    public NutsUpdateCommand all() {
        setUpdateApi(true);
        setUpdateRuntime(true);
        setUpdateExtensions(true);
        setUpdateInstalled(true);
        return this;
    }

    @Override
    public NutsUpdateCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand frozenId(NutsId id) {
        return addFrozenId(id);
    }

    @Override
    public NutsUpdateCommand frozenId(String id) {
        return addFrozenId(id);
    }

    @Override
    public NutsUpdateCommand addFrozenId(NutsId id) {
        if (id != null) {
            frozenIds.add(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addFrozenId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            frozenIds.add(ws.id().parseRequired(id));
        }
        return this;
    }

    @Override
    public NutsUpdateCommand frozenIds(NutsId... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsUpdateCommand frozenIds(String... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsUpdateCommand addFrozenIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addFrozenIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand clearFrozenIds() {
        this.frozenIds.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand includeOptional() {
        return includeOptional(true);
    }

    @Override
    public NutsUpdateCommand includeOptional(boolean enable) {
        return setIncludeOptional(enable);
    }

    @Override
    public NutsUpdateCommand apiVersion(String forceBootAPIVersion) {
        return setApiVersion(forceBootAPIVersion);
    }

    @Override
    public NutsUpdateCommand updateWorkspace() {
        return updateWorkspace(true);
    }

    @Override
    public NutsUpdateCommand updateWorkspace(boolean enable) {
        return setUpdateApi(enable);
    }

    @Override
    public NutsUpdateCommand updateExtensions() {
        return updateExtensions(true);
    }

    @Override
    public NutsUpdateCommand updateExtensions(boolean enable) {
        return setUpdateExtensions(enable);
    }

    @Override
    public NutsUpdateCommand updateRunime() {
        return setUpdateRuntime(true);
    }

    @Override
    public NutsUpdateCommand updateRuntime(boolean enable) {
        return setUpdateRuntime(enable);
    }

    @Override
    public NutsUpdateCommand updateInstalled() {
        return updateInstalled(true);
    }

    @Override
    public NutsUpdateCommand updateInstalled(boolean enable) {
        return setUpdateInstalled(enable);
    }

    @Override
    public NutsUpdateCommand clearScopes() {
        this.scopes.clear();
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "-a":
            case "--all": {
                cmdLine.skip();
                this.all();
                return true;
            }
            case "-w":
            case "--ws":
            case "--workspace": {
                cmdLine.skip();
                this.workspace();
                return true;
            }
            case "-i":
            case "--installed": {
                this.installed(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-r":
            case "--runtime": {
                this.runtime(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-A":
            case "--api": {
                this.runtime(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }

            case "-e":
            case "--extensions": {
                this.extensions(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-c":
            case "--companions": {
                this.companions(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-v":
            case "--api-version":
            case "--to-version": {
                this.setApiVersion(cmdLine.nextString().getStringValue());
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                this.addArgs(cmdLine.toArray());
                cmdLine.skipAll();
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
                    id(a.getString());
                    return true;
                }
            }
        }
    }

}
