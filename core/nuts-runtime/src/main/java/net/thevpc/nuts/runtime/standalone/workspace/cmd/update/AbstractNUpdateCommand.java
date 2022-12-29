/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

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
public abstract class AbstractNUpdateCommand extends NWorkspaceCommandBase<NUpdateCommand> implements NUpdateCommand {

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

    public AbstractNUpdateCommand(NSession ws) {
        super(ws, "update");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public List<NId> getIds() {
        return CoreCollectionUtils.unmodifiableList(ids);
    }

    @Override
    public NUpdateCommand addId(String id) {
        checkSession();
        NSession session = getSession();
        return addId(id == null ? null : NId.of(id).get(session));
    }

    @Override
    public NUpdateCommand addId(NId id) {
        if (id == null) {
            checkSession();
            throw new NNotFoundException(getSession(), id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NUpdateCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUpdateCommand addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUpdateCommand removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NUpdateCommand removeId(String id) {
        checkSession();
        NSession session = getSession();
        return removeId(NId.of(id).get(session));
    }

    @Override
    public NUpdateCommand addScope(NDependencyScope scope) {
        if (scope != null) {
            scopes.add(scope);
        }
        return this;
    }

    @Override
    public NUpdateCommand addScopes(NDependencyScope... scopes) {
        if (scopes != null) {
            for (NDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public NUpdateCommand addScopes(Collection<NDependencyScope> scopes) {
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
    public NUpdateCommand setOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NUpdateCommand addArg(String arg) {
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
    public NUpdateCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NUpdateCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NUpdateCommand addArgs(Collection<String> args) {
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
    public NUpdateCommand setEnableInstall(boolean enableInstall) {
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
            if (id.getShortName().equals(ws.getRuntimeId().getShortName())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public NUpdateCommand setApi(boolean enableMajorUpdates) {
        this.updateApi = enableMajorUpdates;
        return this;
    }

    @Override
    public NUpdateCommand setCompanions(boolean updateCompanions) {
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
    public NUpdateCommand setExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public NVersion getApiVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NUpdateCommand setApiVersion(NVersion value) {
        this.forceBootAPIVersion = value;
        return this;
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NWorkspaceUpdateResult getResult() {
        checkSession();
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NUnexpectedException(getSession());
        }
        return result;
    }

    @Override
    public NUpdateCommand run() {
        return update();
    }

    @Override
    public NUpdateCommand checkUpdates(boolean applyUpdates) {
        checkUpdates();
        if (applyUpdates) {
            update();
        }
        return this;
    }

    @Override
    public NUpdateCommand setRuntime(boolean updateRuntime) {
        this.updateRuntime = updateRuntime;
        return this;
    }

    @Override
    public NUpdateCommand setInstalled(boolean enable) {
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
    public NUpdateCommand setAll() {
        setApi(true);
        setRuntime(true);
        setExtensions(true);
        setCompanions(true);
        setInstalled(true);
        return this;
    }

    @Override
    public NUpdateCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NUpdateCommand addLockedId(NId id) {
        if (id != null) {
            lockedIds.add(id);
        }
        return this;
    }

    @Override
    public NUpdateCommand addLockedId(String id) {
        checkSession();
        NSession session = getSession();
        if (!NBlankable.isBlank(id)) {
            lockedIds.add(NId.of(id).get(session));
        }
        return this;
    }

    @Override
    public NUpdateCommand addLockedIds(NId... ids) {
        if (ids != null) {
            for (NId id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NUpdateCommand addLockedIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NUpdateCommand clearLockedIds() {
        this.lockedIds.clear();
        return this;
    }

    @Override
    public NUpdateCommand clearScopes() {
        this.scopes.clear();
        return this;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        checkSession();
        NArgument a = cmdLine.peek().get(session);
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
                cmdLine.withNextBoolean((v, r, s) -> this.setInstalled(v));
                return true;
            }
            case "-r":
            case "--runtime": {
                cmdLine.withNextBoolean((v, r, s) -> this.setRuntime(v));
                return true;
            }
            case "-A":
            case "--api": {
                cmdLine.withNextBoolean((v, r, s) -> this.setApi(v));
                return true;
            }

            case "-e":
            case "--extensions": {
                cmdLine.withNextBoolean((v, r, s) -> this.setExtensions(v));
                return true;
            }
            case "-c":
            case "--companions": {
                cmdLine.withNextBoolean((v, r, s) -> this.setCompanions(v));
                return true;
            }
            case "-v":
            case "--api-version":
            case "--to-version": {
                cmdLine.withNextString((v, r, s) -> this.setApiVersion(NVersion.of(v).get(getSession())));
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
                a = cmdLine.next().get(session);
                if (enabled) {
                    if (a.getStringValue() != null) {
                        expireTime = Instant.parse(a.getStringValue().get(session));
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
                    addId(a.asString().get(session));
                    return true;
                }
            }
        }
    }

}
