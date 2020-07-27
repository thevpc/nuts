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
package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;

import java.util.*;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public abstract class AbstractNutsInstallCommand extends NutsWorkspaceCommandBase<NutsInstallCommand> implements NutsInstallCommand {


    protected boolean defaultVersion = true;
    protected boolean companions = false;
    protected boolean installed = false;
    protected List<String> args;
    protected final List<NutsId> ids = new ArrayList<>();
    protected NutsDefinition[] result;
    protected NutsId[] fails;

    public AbstractNutsInstallCommand(NutsWorkspace ws) {
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
        return addId(id == null ? null : ws.id().parse(id));
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
            this.ids.remove(ws.id().parse(id));
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
    public boolean isCompanions() {
        return companions;
    }

    @Override
    public NutsInstallCommand setCompanions(boolean value) {
        this.companions = value;
        return this;
    }

    @Override
    public boolean isInstalled() {
        return installed;
    }

    @Override
    public NutsInstallCommand setInstalled(boolean value) {
        this.installed = value;
        return this;
    }

    @Override
    public NutsInstallCommand installed(boolean value) {
        return setInstalled(value);
    }

    @Override
    public NutsInstallCommand installed() {
        return installed(true);
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
    public NutsInstallCommand companions(boolean value) {
        return setCompanions(value);
    }

    @Override
    public NutsInstallCommand companions() {
        return companions(true);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled=a.isEnabled();
        switch (a.getStringKey()) {
            case "-c":
            case "--companions": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setCompanions(val);
                }
                return true;
            }
            case "-i":
            case "--installed": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.installed(val);
                }
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                if (enabled) {
                    this.addArgs(cmdLine.toArray());
                }
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
