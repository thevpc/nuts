/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallCommand;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreCommonUtils;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand implements NutsInstallCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsInstallCommand.class.getName());

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean defaultVersion = true;
    private boolean includecompanions = false;
    private List<String> args;
    private final List<NutsId> ids = new ArrayList<>();
    private NutsSession session;
    private final NutsWorkspace ws;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsInstallCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsInstallCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsInstallCommand setId(String id) {
        return setId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsInstallCommand setId(NutsId id) {
        if (id == null) {
            ids.clear();
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsInstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsInstallCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsInstallCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    @Override
    public boolean isAsk() {
        return ask;
    }

    @Override
    public NutsInstallCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsInstallCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsInstallCommand setArgs(List<String> args) {
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

    @Override
    public NutsInstallCommand addArg(String arg) {
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
    public NutsInstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsInstallCommand addArgs(Collection<String> args) {
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
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsInstallCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public boolean isIncludecompanions() {
        return includecompanions;
    }

    @Override
    public NutsInstallCommand setIncludeCompanions(boolean includecompanions) {
        this.includecompanions = includecompanions;
        return this;
    }

    @Override
    public NutsInstallCommand args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NutsInstallCommand args(String... args) {
        return addArgs(args);
    }

    @Override
    public NutsInstallCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    @Override
    public NutsInstallCommand ids(String... ids) {
        return addIds(ids);
    }

    @Override
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    @Override
    public NutsInstallCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsInstallCommand trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsInstallCommand trace() {
        return setTrace(true);
    }

    @Override
    public NutsInstallCommand setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    @Override
    public NutsInstallCommand defaultVersion(boolean defaultVersion) {
        return setDefaultVersion(defaultVersion);
    }

    @Override
    public NutsInstallCommand defaultVersion() {
        return defaultVersion(true);
    }

    @Override
    public NutsInstallCommand includeCompanions(boolean includecompanions) {
        return setIncludeCompanions(includecompanions);
    }

    @Override
    public NutsInstallCommand includecompanions() {
        return includeCompanions(true);
    }

    @Override
    public NutsDefinition[] install() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = CoreNutsUtils.validateSession(this.getSession(), ws);
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "install");

        if (this.isIncludecompanions()) {
            boolean companions = true;
            if (ws.config().getOptions().isYes()) {
                //ok;
            } else if (ws.config().getOptions().isNo()) {
                //ok;
                companions = false;
            } else {
                NutsQuestion<Boolean> q = NutsQuestion.forBoolean("Would you like to install recommended companion tools").setDefautValue(true);
                if (this.isAsk() && !ws.getTerminal().ask(q)) {
                    companions = false;
                }
            }
            if (companions) {
                String[] companionTools = dws.getCompanionTools();
                if (companionTools.length > 0) {
                    long cr = System.currentTimeMillis();
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.FINE, "Installing companion tools...");
                    }
                    int companionCount = 0;
//        NutsCommandExecBuilder e = createExecBuilder();
//        e.addCommand("net.vpc.app.nuts.toolbox:ndi","install","-f");
                    for (String companionTool : companionTools) {
                        if (this.isForce() || !dws.isInstalled(ws.parser().parseRequiredId(companionTool), false, session)) {
                            if (this.isTrace()) {
                                if (companionCount == 0) {
                                    out.println("Installation of Nuts companion tools...");
                                }
                                String d = ws.fetch().id(companionTool).getResultDescriptor().getDescription();
                                out.printf("##\\### Installing ==%s== (%s)...\n", companionTool, d);
                            }
                            if (log.isLoggable(Level.CONFIG)) {
                                log.log(Level.FINE, "Installing companion tool : {0}", companionTool);
                            }
                            ws.install().id(companionTool).setArgs("--!silent", "--force").setForce(true).setSession(session).install();
                            companionCount++;
                        }
//            e.addCommand(companionTool.getId());
                    }
                    if (companionCount > 0) {
//            e.exec();
                        if (this.isTrace()) {
                            out.printf("Installation of ==%s== companion tools in ==%s== ##succeeded##...\n", companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis() - cr));
                        }
                    } else {
                        out.print("All companion tools are already installed...\n");
                    }
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.FINE, "Installed {0} companion tools in {1}...", new Object[]{companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis() - cr)});
                    }
                }
            }
        }

        List<NutsDefinition> defsAll = new ArrayList<>();
        List<NutsDefinition> defsToInstall = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            NutsDefinition def = ws.fetch().id(id).session(session).setAcceptOptional(false).includeDependencies().setIncludeInstallInformation(true).getResultDefinition();
            if (def != null && def.getPath() != null) {
                boolean installed = false;
                if (def.getInstallation().isInstalled()) {
                    if (!this.isForce()) {
                        if (this.isTrace()) {
                            out.printf(ws.formatter().createIdFormat().toString(def.getId()) + " already installed\n");
                        }
                        installed = true;
                    }
                }
                if (!installed) {
                    defsToInstall.add(def);
                }
                defsAll.add(def);
            }
        }
        for (NutsDefinition def : defsToInstall) {
            dws.installImpl(def, this.getArgs(), null, session, true, this.isTrace());
        }
        if (isDefaultVersion()) {
            for (NutsDefinition nutsDefinition : defsAll) {
                dws.getInstalledRepository().setDefaultVersion(nutsDefinition.getId());
                if (this.isTrace()) {
                    out.printf("Set default version as ==%s==...\n", nutsDefinition.getId());
                }
            }
        }
        return defsAll.toArray(new NutsDefinition[0]);
    }
}
