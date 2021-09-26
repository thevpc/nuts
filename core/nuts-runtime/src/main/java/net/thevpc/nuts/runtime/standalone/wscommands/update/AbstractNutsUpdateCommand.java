/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsWorkspaceCommandBase;

import java.time.Instant;
import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNutsUpdateCommand extends NutsWorkspaceCommandBase<NutsUpdateCommand> implements NutsUpdateCommand {

    protected boolean enableInstall = true;
    protected boolean updateApi = false;
    protected boolean updateRuntime = false;
    protected boolean updateExtensions = false;
    protected boolean updateInstalled = false;
    protected boolean updateCompanions = false;
    protected boolean includeOptional = false;
    protected NutsVersion forceBootAPIVersion;
    protected Instant expireTime;
    protected List<String> args;
    protected final List<NutsDependencyScope> scopes = new ArrayList<>();
    protected final List<NutsId> lockedIds = new ArrayList<>();
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
    public NutsUpdateCommand addId(String id) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        return addId(id == null ? null : ws.id().parser().parse(id));
    }

    @Override
    public NutsUpdateCommand addId(NutsId id) {
        if (id == null) {
            checkSession();
            throw new NutsNotFoundException(getSession(), id);
        } else {
            ids.add(id);
        }
        return this;
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
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        return removeId(ws.id().parser().parse(id));
    }

    @Override
    public NutsUpdateCommand addScope(NutsDependencyScope scope) {
        if (scope != null) {
            scopes.add(scope);
        }
        return this;
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
    public boolean isOptional() {
        return includeOptional;
    }

    @Override
    public NutsUpdateCommand setOptional(boolean includeOptional) {
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
    public NutsId[] getLockedIds() {
        return lockedIds == null ? new NutsId[0] : lockedIds.toArray(new NutsId[0]);
    }

    @Override
    public boolean isEnableInstall() {
        return enableInstall;
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
    public boolean isApi() {
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
        for (NutsId id : ids) {
            if (id.getShortName().equals(ws.getRuntimeId().getShortName())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public NutsUpdateCommand setApi(boolean enableMajorUpdates) {
        this.updateApi = enableMajorUpdates;
        return this;
    }

    @Override
    public NutsUpdateCommand setCompanions(boolean updateCompanions) {
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
    public NutsUpdateCommand setExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public NutsVersion getApiVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NutsUpdateCommand setApiVersion(NutsVersion value) {
        this.forceBootAPIVersion = value;
        return this;
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NutsWorkspaceUpdateResult getResult() {
        checkSession();
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NutsUnexpectedException(getSession());
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
    public NutsUpdateCommand setRuntime(boolean updateRuntime) {
        this.updateRuntime = updateRuntime;
        return this;
    }

    @Override
    public NutsUpdateCommand setInstalled(boolean enable) {
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
    public NutsUpdateCommand setAll() {
        setApi(true);
        setRuntime(true);
        setExtensions(true);
        setCompanions(true);
        setInstalled(true);
        return this;
    }

    @Override
    public NutsUpdateCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand addLockedId(NutsId id) {
        if (id != null) {
            lockedIds.add(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addLockedId(String id) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (!NutsBlankable.isBlank(id)) {
            lockedIds.add(ws.id().parser().setLenient(false).parse(id));
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addLockedIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addLockedIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand clearLockedIds() {
        this.lockedIds.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand clearScopes() {
        this.scopes.clear();
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        checkSession();
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getKey().getString()) {
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
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setInstalled(val);
                }
                return true;
            }
            case "-r":
            case "--runtime": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setRuntime(val);
                }
                return true;
            }
            case "-A":
            case "--api": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setApi(val);
                }
                return true;
            }

            case "-e":
            case "--extensions": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setExtensions(val);
                }
                return true;
            }
            case "-c":
            case "--companions": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setCompanions(val);
                }
                return true;
            }
            case "-v":
            case "--api-version":
            case "--to-version": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    this.setApiVersion(getSession().getWorkspace().version().parse(val));
                }
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
                a = cmdLine.next();
                if (enabled) {
                    if (a.getValue().getString() != null) {
                        expireTime=Instant.parse(a.getValue().getString());
                    } else {
                        expireTime=Instant.now();
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
                    addId(a.getString());
                    return true;
                }
            }
        }
    }

}
