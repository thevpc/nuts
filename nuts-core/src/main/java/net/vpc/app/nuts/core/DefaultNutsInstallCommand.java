/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.vpc.app.nuts.core.util.CorePlatformUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand implements NutsInstallCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsInstallCommand.class.getName());

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean includecompanions = false;
    private List<String> args;
    private List<NutsId> ids = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsInstallCommand id(String id) {
        return setId(id);
    }

    public NutsInstallCommand id(NutsId id) {
        return setId(id);
    }

    public NutsInstallCommand setId(String id) {
        return setId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsInstallCommand setId(NutsId id) {
        if (id == null) {
            ids.clear();
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsInstallCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsInstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsInstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    public NutsInstallCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsInstallCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsInstallCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    public boolean isAsk() {
        return ask;
    }

    public NutsInstallCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    public NutsInstallCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

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

    public NutsInstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    public NutsInstallCommand addArgs(List<String> args) {
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

    public NutsInstallCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    public boolean isIncludecompanions() {
        return includecompanions;
    }

    public NutsInstallCommand setIncludecompanions(boolean includecompanions) {
        this.includecompanions = includecompanions;
        return this;
    }

    public NutsDefinition[] install() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsInstallCommand options = this;
        NutsSession session = CoreNutsUtils.validateSession(options.getSession(), ws);
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "install");

        if (options.isIncludecompanions()) {
            boolean companions = true;
            if (ws.config().getOptions().isYes()) {
                //ok;
            } else if (ws.config().getOptions().isNo()) {
                //ok;
                companions = false;
            } else {
                NutsQuestion<Boolean> q = NutsQuestion.forBoolean("Would you like to install recommended companion tools").setDefautValue(true);
                if (options.isAsk() && !ws.getTerminal().ask(q)) {
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
                        if (options.isForce() || !dws.isInstalled(ws.parser().parseRequiredId(companionTool), false, session)) {
                            if (options.isTrace()) {
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
                        if (options.isTrace()) {
                            out.printf("Installation of ==%s== companion tools in ==%s== ##succeeded##...\n", companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis()-cr));
                        }
                    } else {
                        out.print("All companion tools are already installed...\n");
                    }
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.FINE, "Installed {0} companion tools in {1}...", new Object[]{companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis()-cr)});
                    }
                }
            }
        }

        List<NutsDefinition> defsAll = new ArrayList<>();
        List<NutsDefinition> defsToInstall = new ArrayList<>();
        for (NutsId id : options.getIds()) {
            NutsDefinition def = ws.fetch().id(id).session(session).setAcceptOptional(false).includeDependencies().setIncludeInstallInformation(true).getResultDefinition();
            if (def != null && def.getPath() != null) {
                boolean installed = false;
                if (def.getInstallation().isInstalled()) {
                    if (!options.isForce()) {
                        if (options.isTrace()) {
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
            dws.installImpl(def, options.getArgs(), null, session, true, options.isTrace());
        }
        return defsAll.toArray(new NutsDefinition[0]);
    }
}
