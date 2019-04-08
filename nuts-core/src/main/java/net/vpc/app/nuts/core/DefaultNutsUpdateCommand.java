/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsUpdateCommand;
import net.vpc.app.nuts.NutsUpdateResult;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateCommand implements NutsUpdateCommand {

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean enableInstall = true;
    private List<String> args;
    private List<NutsId> ids = new ArrayList<>();
    private List<NutsId> frozenIds = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;

    public DefaultNutsUpdateCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsUpdateCommand id(String id) {
        return setId(id);
    }

    public NutsUpdateCommand id(NutsId id) {
        return setId(id);
    }

    public NutsUpdateCommand setId(String id) {
        return setId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsUpdateCommand setId(NutsId id) {
        if (id == null) {
            ids.clear();
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsUpdateCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsUpdateCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsUpdateCommand ids(String... ids) {
        return addIds(ids);
    }

    public NutsUpdateCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    public NutsUpdateCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    public NutsUpdateCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsUpdateCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsUpdateCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    public boolean isAsk() {
        return ask;
    }

    public NutsUpdateCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    public NutsUpdateCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

    public NutsUpdateCommand setArgs(List<String> args) {
        this.args = new ArrayList<>();
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

    public NutsUpdateCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    public NutsUpdateCommand addArgs(List<String> args) {
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

    public NutsSession getSession() {
        return session;
    }

    public NutsUpdateCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsId[] getFrozenIds() {
        return frozenIds == null ? new NutsId[0] : frozenIds.toArray(new NutsId[0]);
    }

    public boolean isEnableInstall() {
        return enableInstall;
    }

    public NutsUpdateCommand setEnableInstall(boolean enableInstall) {
        this.enableInstall = enableInstall;
        return this;
    }

    public NutsUpdateResult[] update() {
        return update(true);
    }

    public NutsUpdateResult[] checkUpdates() {
        return update(false);
    }

    @Override
    public NutsUpdateResult[] checkUpdates(boolean applyUpdates) {
        return update(applyUpdates);
    }

    public NutsUpdateResult[] update(boolean applyUpdates) {
        Map<String, NutsUpdateResult> all = new HashMap<>();
        for (NutsId id : new HashSet<>(Arrays.asList(this.getIds()))) {
            NutsUpdateResult updated = update(id, applyUpdates);
            all.put(updated.getId().getSimpleName(), updated);
        }
        NutsId[] frozenIds = this.getFrozenIds();
        if (frozenIds.length > 0) {
            for (NutsId d : new HashSet<>(Arrays.asList(frozenIds))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d.toString());
                if (all.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = all.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return all.values().toArray(new NutsUpdateResult[0]);
    }

//    @Override
    protected NutsUpdateResult update(NutsId id, boolean applyUpdates) {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = CoreNutsUtils.validateSession(this.getSession(), ws);
//        if (options == null) {
//            options = new NutsUpdateOptions();
//        }
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "update");
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }

        NutsUpdateResult r = new NutsUpdateResult().setId(id.getSimpleNameId());

        final PrintStream out = CoreIOUtils.resolveOut(ws, session);
        NutsDefinition d0 = ws.fetch().id(id).setSession(session).offline().setAcceptOptional(false).setLenient(true).getResultDefinition();
        NutsDefinition d1 = ws.fetch().id(id).setSession(session).setAcceptOptional(false).includeDependencies().setLenient(true).getResultDefinition();
        r.setLocalVersion(d0);
        r.setAvailableVersion(d1);
        final String simpleName = d0 != null ? d0.getId().getSimpleName() : d1 != null ? d1.getId().getSimpleName() : id.getSimpleName();
        if (d0 == null) {
            if (!this.isEnableInstall()) {
                throw new NutsIllegalArgumentException("No version is installed to be updated for " + id);
            }
            if (d1 == null) {
                throw new NutsNotFoundException(id);
            }
            r.setUpdateAvailable(true);
            r.setUpdateForced(false);
            if (applyUpdates) {
                dws.installImpl(d1, new String[0], null, session, true, this.isTrace());
                r.setUpdateApplied(true);
                if (this.isTrace()) {
                    out.printf("==%s== is [[forced]] to latest version ==%s==\n", simpleName, d1.getId().getVersion());
                }
            } else {
                if (this.isTrace()) {
                    out.printf("==%s== is [[not-installed]] . New version is available ==%s==\n", simpleName, d1.getId().getVersion());
                }
            }
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
            r.setAvailableVersion(d0);
            if (this.isTrace()) {
                out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", d0.getId().getSimpleName(), d0.getId().getVersion());
            }
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (this.isForce()) {
                    if (applyUpdates) {
                        dws.installImpl(d1, new String[0], null, session, true, this.isTrace());
                        r.setUpdateApplied(true);
                        r.setUpdateForced(true);
                        if (this.isTrace()) {
                            out.printf("==%s== is [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    } else {
                        r.setUpdateForced(true);
                        if (this.isTrace()) {
                            out.printf("==%s== would be [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    }
                } else {
                    if (this.isTrace()) {
                        out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", simpleName, d0.getId().getVersion());
                    }
                }
            } else {
                r.setUpdateAvailable(true);
                if (applyUpdates) {
                    dws.installImpl(d1, new String[0], null, session, true, this.isTrace());
                    r.setUpdateApplied(true);
                    if (this.isTrace()) {
                        out.printf("==%s== is [[updated]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                } else {
                    if (this.isTrace()) {
                        out.printf("==%s== is [[updatable]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            }
        }
        return r;
    }

}
