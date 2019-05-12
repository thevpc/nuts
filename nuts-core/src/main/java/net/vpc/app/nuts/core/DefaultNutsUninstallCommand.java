/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefaultCommandLine;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsUninstallCommand;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsArgument;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsUninstallCommand extends NutsWorkspaceCommandBase<NutsUninstallCommand> implements NutsUninstallCommand {

    private boolean erase = false;
    private List<String> args;
    private final List<NutsId> ids = new ArrayList<>();

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUninstallCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsUninstallCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsUninstallCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsUninstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsUninstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUninstallCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUninstallCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsUninstallCommand removeId(String id) {
        return removeId(ws.parser().parseId(id));
    }

    @Override
    public NutsUninstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsUninstallCommand arg(String arg) {
        return addArg(arg);
    }

    @Override
    public NutsUninstallCommand args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NutsUninstallCommand args(String... args) {
        return addArgs(args);
    }

    @Override
    public NutsUninstallCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsUninstallCommand erase() {
        return setErase(true);
    }

    @Override
    public NutsUninstallCommand erase(boolean erase) {
        return setErase(erase);
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsUninstallCommand addArg(String arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NutsUninstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsUninstallCommand addArgs(Collection<String> args) {
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
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsUninstallCommand run() {
        NutsWorkspaceUtils.checkReadOnly(ws);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        ws.security().checkAllowed(NutsConstants.Rights.UNINSTALL, "uninstall");
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            NutsDefinition def = ws.fetch().id(id).setSession(session.copy()).setTransitive(false).setAcceptOptional(false).includeDependencies()
                    .setIncludeInstallInformation(true).getResultDefinition();
            if (!def.getInstallation().isInstalled()) {
                throw new NutsIllegalArgumentException(id + " Not Installed");
            }
            defs.add(def);
        }
        for (NutsDefinition def : defs) {
            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), ws.fetch().session(session));
            NutsInstallerComponent ii = dws.getInstaller(def, session);
            PrintStream out = CoreIOUtils.resolveOut(ws, session);
            if (ii != null) {
//        NutsDescriptor descriptor = nutToInstall.getDescriptor();
                NutsExecutionContext executionContext = dws.createNutsExecutionContext(def, this.getArgs(), new String[0], session, true, null);
                ii.uninstall(executionContext, this.isErase());
                try {
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.PROGRAMS).toFile());
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.LOGS).toFile());
                    if (this.isErase()) {
                        CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                        CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                if (getValidSession().isTrace()) {
                    out.printf("%N uninstalled ##successfully##%n", ws.formatter().createIdFormat().toString(id));
                }
            } else {
                if (getValidSession().isTrace()) {
                    out.printf("%N @@could not@@ be uninstalled%n", ws.formatter().createIdFormat().toString(id));
                }
            }
        }
        return this;
    }

    @Override
    public boolean isErase() {
        return erase;
    }

    @Override
    public NutsUninstallCommand setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public NutsUninstallCommand parseOptions(String... args) {
        NutsCommandLine cmd = ws.parser().parseCommandLine(args);
        NutsArgument a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "-e":
                case "--earse": {
                    this.setErase(a.getBooleanValue());
                    break;
                }
                case "-g":
                case "--args": {
                    while (cmd.hasNext()) {
                        this.addArg(cmd.next().getString());
                    }
                    break;
                }
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("Unsupported option " + a);
                        } else {
                            id(a.getString());
                        }
                    }
                }
            }
        }
        return this;
    }

}
