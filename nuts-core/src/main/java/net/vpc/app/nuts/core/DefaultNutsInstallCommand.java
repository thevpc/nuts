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
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsInstallCommand;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * type: Command Class
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
    private NutsDefinition[] result;
    private NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsInstallCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsInstallCommand id(NutsId id) {
        return addId(id);
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
    public NutsInstallCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand removeId(String id) {
        if (id != null) {
            this.ids.remove(ws.parser().parseId(id));
        }
        return this;
    }

    @Override
    public NutsInstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsInstallCommand arg(String arg) {
        this.addArg(arg);
        return this;
    }

    @Override
    public NutsInstallCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsInstallCommand ask() {
        return setAsk(true);
    }

    @Override
    public NutsInstallCommand ask(boolean ask) {
        return setAsk(ask);
    }

    @Override
    public NutsInstallCommand force() {
        return setForce(force);
    }

    @Override
    public NutsInstallCommand force(boolean force) {
        return setForce(force);
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
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
    public boolean isIncludeCompanions() {
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
    public NutsInstallCommand includeCompanions() {
        return includeCompanions(true);
    }

    @Override
    public NutsInstallCommand parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.getKey().getString()) {
                case "-f":
                case "--force": {
                    this.setForce(a.getBooleanValue());
                    break;
                }
                case "-t": 
                case "--trace": {
                    this.setTrace(a.getBooleanValue());
                    break;
                }
                case "-c": 
                case "--companions": 
                {
                    this.setIncludeCompanions(a.getBooleanValue());
                    break;
                }
                case "-g": 
                case "--args": 
                {
                    while ((a = cmd.next()) != null) {
                        this.addArg(a.getString());
                    }
                    break;
                }
                case "--trace-format": {
                    this.setOutputFormat(NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                    break;
                }
                case "--json": {
                    this.setOutputFormat(NutsOutputFormat.JSON);
                    break;
                }
                case "--props": {
                    this.setOutputFormat(NutsOutputFormat.PROPS);
                    break;
                }
                case "--plain": {
                    this.setOutputFormat(NutsOutputFormat.PLAIN);
                    break;
                }
                default: {
                    if (a.isOption()) {
                        throw new NutsIllegalArgumentException("Unsupported option " + a);
                    } else {
                        id(a.getString());
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NutsInstallCommand run() {
        boolean emptyCommand = true;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "install");

        if (this.isIncludeCompanions()) {
            emptyCommand = false;
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
                                String d = ws.find().id(companionTool).latestVersions()
                                        .getResultDefinitions().required().getDescriptor().getDescription();
                                out.printf("##\\### Installing ==%s== (%s)...\n", companionTool, d);
                            }
                            if (log.isLoggable(Level.CONFIG)) {
                                log.log(Level.FINE, "Installing companion tool : {0}", companionTool);
                            }
                            ws.install().id(companionTool).args("--trace", "--force").setForce(true).setSession(session).run();
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
            emptyCommand = false;
            NutsDefinition def = ws.find().id(id).session(session).setAcceptOptional(false)
                    .includeDependencies().scope(NutsDependencyScope.PROFILE_RUN).includeInstallInformation().latestVersions().getResultDefinitions().required();
            if (def != null && def.getPath() != null) {
                boolean installed = false;
                if (def.getInstallation().isInstalled()) {
                    if (!this.isForce()) {
                        if (this.isTrace()) {
                            out.printf("%N already installed\n", ws.formatter().createIdFormat().toString(def.getId()));
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
            dws.installImpl(def, this.getArgs(), null, session, true, this.isTrace(), isDefaultVersion());
        }
        if (emptyCommand) {
            throw new NutsExecutionException("Missing components to install", 1);
        }
        result = defsAll.toArray(new NutsDefinition[0]);
        return this;
    }

    @Override
    public int getResultCount() {
        return getResult().length;
    }

    @Override
    public NutsDefinition[] getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public DefaultNutsInstallCommand outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    @Override
    public DefaultNutsInstallCommand setOutputFormat(NutsOutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsOutputFormat.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public DefaultNutsInstallCommand json() {
        return setOutputFormat(NutsOutputFormat.JSON);
    }

    @Override
    public DefaultNutsInstallCommand plain() {
        return setOutputFormat(NutsOutputFormat.PLAIN);
    }

    @Override
    public DefaultNutsInstallCommand props() {
        return setOutputFormat(NutsOutputFormat.PROPS);
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return this.outputFormat;
    }
}
