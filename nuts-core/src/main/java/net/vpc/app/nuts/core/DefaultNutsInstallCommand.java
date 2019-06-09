/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallCommand;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsConfirmationMode;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand extends NutsWorkspaceCommandBase<NutsInstallCommand> implements NutsInstallCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsInstallCommand.class.getName());

    private boolean defaultVersion = true;
    private boolean includecompanions = false;
    private List<String> args;
    private final List<NutsId> ids = new ArrayList<>();
    private NutsDefinition[] result;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        super(ws, "install");
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
        return addId(id == null ? null : ws.parse().id(id));
    }

    @Override
    public NutsInstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
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
    public NutsInstallCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand removeId(String id) {
        if (id != null) {
            this.ids.remove(ws.parse().id(id));
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
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "-c":
            case "--companions": {
                this.setIncludeCompanions(cmdLine.nextBoolean().getBooleanValue());
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

    @Override
    public NutsInstallCommand run() {
        boolean emptyCommand = true;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getValidSession();
        NutsSession searchSession = session.copy().trace(false);
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "install");
        if (this.isIncludeCompanions()) {
            emptyCommand = false;
            if (ws.io().getTerminal().ask()
                    .forBoolean("The following ==nuts== companion tools are going to be installed : " + 
                        Arrays.stream(dws.getCompanionTools())
                                .map(x->ws.format().id().setOmitImportedGroup(true).toString(ws.parse().id(x)))
                                .collect(Collectors.joining(", "))
                        +"%nAccept"
                        
                )
                    .defaultValue(true)
                    .session(session).getValue()) {
                String[] companionTools = dws.getCompanionTools();
                if (companionTools.length > 0) {
                    long cr = System.currentTimeMillis();
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.FINE, "Installing companion tools...");
                    }
                    int companionCount = 0;
//        NutsCommandExecBuilder e = createExecBuilder();
//        e.addCommand("net.vpc.app.nuts.toolbox:ndi","install","-f");
                    for (String companionTool : companionTools) {
                        if (session.isForce() || !dws.isInstalled(ws.parse().requiredId(companionTool), false, session)) {
                            NutsDefinition r = null;
                            if (session.isTrace()) {
                                if (companionCount == 0) {
                                    if (session.isPlainTrace()) {
                                        out.println("Installing Nuts companion tools...");
                                    }
                                }
                                NutsId rId = ws.search().session(searchSession).id(companionTool).latest().getResultIds().required();
                                r = ws.fetch().session(searchSession).id(rId).failFast().getResultDefinition();
                                String d = r.getDescriptor().getDescription();
                                if (session.isPlainTrace()) {
                                    out.printf("##\\### Installing ==%s== (%s)...%n", r.getId().getLongName(), d);
                                }
                            } else {
                                NutsId rId = ws.search().session(searchSession).id(companionTool).latest().getResultIds().required();
                                r = ws.fetch().session(searchSession).id(rId).failFast().getResultDefinition();
                            }
                            if (LOG.isLoggable(Level.CONFIG)) {
                                LOG.log(Level.FINE, "Installing companion tool : {0}", r.getId().getLongName());
                            }
                            NutsInstallCommand companionInstall = ws.install().id(companionTool)
                                    .setSession(session.copy().confirm(NutsConfirmationMode.YES));
                            if (session.isTrace()) {
                                companionInstall.args("--trace");
                            }
                            if (session.isForce()) {
                                companionInstall.args("--force");
                            }
                            companionInstall.run();
                            companionCount++;
                        }
                    }
                    if (companionCount > 0) {
                        if (getValidSession().isPlainTrace()) {
                            out.printf("Installation of ==%s== companion tools in ==%s== ##succeeded##...%n", companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis() - cr));
                        }
                    } else {
                        if (getValidSession().isPlainTrace()) {
                            out.println("All companion tools are already installed...");
                        }
                    }
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.FINE, "Installed {0} companion tools in {1}...", new Object[]{companionCount, CoreCommonUtils.formatPeriodMilli(System.currentTimeMillis() - cr)});
                    }
                }
            }
        }

        List<NutsDefinition> defsAll = new ArrayList<>();
        List<NutsDefinition> defsToInstall = new ArrayList<>();
        List<NutsDefinition> defsToDefVersion = new ArrayList<>();
        List<NutsDefinition> defsToIgnore = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            emptyCommand = false;
            List<NutsId> allIds = ws.search().id(id).session(searchSession).latest().getResultIds().list();
            if (allIds.isEmpty()) {
                throw new NutsNotFoundException(ws, id);
            }
            for (NutsId nid : allIds) {
                NutsDefinition def = ws.fetch().id(nid).session(searchSession)
                        .installInformation().getResultDefinition();
                if (def != null && def.getPath() != null) {
                    boolean installed = def.getInstallation().isInstalled();
                    boolean defVer = NutsWorkspaceExt.of(ws).getInstalledRepository().isDefaultVersion(def.getId());
                    if (!installed || getValidSession().isForce()) {
                        defsToInstall.add(def);
                    } else if (!defVer) {
                        defsToDefVersion.add(def);
                    } else {
                        defsToIgnore.add(def);
                    }
                    defsAll.add(def);
                }
            }
        }
        for (NutsDefinition def : defsToIgnore) {
            if (getValidSession().isPlainTrace()) {
                out.printf("%N already installed%n", ws.format().id().toString(def.getId()));
            }
        }
        if (!defsToInstall.isEmpty() && ws.io().getTerminal().ask()
                .forBoolean("The following ==nuts== components are going to be installed : " + 
                        defsToInstall.stream()
                                .map(x->ws.format().id().setOmitImportedGroup(true).toString(x.getId().getLongNameId()))
                                .collect(Collectors.joining(", "))
                        +"%nAccept"
                        
                )
                .defaultValue(true)
                .session(session).getBooleanValue()) {
            for (NutsDefinition def : defsToInstall) {
                dws.installImpl(def, this.getArgs(), null, session, isDefaultVersion());
            }
        }
        for (NutsDefinition def : defsToDefVersion) {
            dws.getInstalledRepository().setDefaultVersion(def.getId());
            if (getValidSession().isPlainTrace()) {
                out.printf("%N already ==installed==. Set as ##default##.%n", ws.format().id().toString(def.getId()));
            }
        }
        if (emptyCommand) {
            throw new NutsExecutionException(ws, "Missing components to install", 1);
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
}
