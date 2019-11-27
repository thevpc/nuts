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
 * @author vpc
 */
public abstract class AbstractDefaultNutsPushCommand extends NutsWorkspaceCommandBase<NutsPushCommand> implements NutsPushCommand {

    protected boolean offline = false;
    protected List<String> args;
    protected final List<NutsId> ids = new ArrayList<>();
    protected List<NutsId> frozenIds;
    protected String repository;

    public AbstractDefaultNutsPushCommand(NutsWorkspace ws) {
        super(ws, "push");
    }

    @Override
    public NutsPushCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsPushCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsPushCommand addId(String id) {
        return addId(id == null ? null : ws.id().parseRequired(id));
    }

    @Override
    public NutsPushCommand addFrozenId(String id) {
        return addFrozenId(id == null ? null : ws.id().parseRequired(id));
    }

    @Override
    public NutsPushCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand removeId(NutsId id) {
        if (id != null) {
            ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand removeId(String id) {
        if (id != null) {
            ids.remove(ws.id().parse(id));
        }
        return this;
    }

    @Override
    public NutsPushCommand removeFrozenId(NutsId id) {
        if (id != null) {
            if (frozenIds != null) {
                frozenIds.remove(id);
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand removeFrozenId(String id) {
        if (id != null) {
            if (frozenIds != null) {
                frozenIds.remove(ws.id().parse(id));
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand addFrozenId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
        } else {
            if (frozenIds == null) {
                frozenIds = new ArrayList<>();
            }
            frozenIds.add(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand ids(String... ids) {
        return addIds(ids);
    }

    @Override
    public NutsPushCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    @Override
    public NutsPushCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addFrozenIds(String... ids) {
        for (String id : ids) {
            addFrozenId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addFrozenIds(NutsId... ids) {
        for (NutsId id : ids) {
            addFrozenId(id);
        }
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsPushCommand addArg(String arg) {
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
    public NutsPushCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsPushCommand addArgs(Collection<String> args) {
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
    public NutsId[] getFrozenIds() {
        return frozenIds == null ? new NutsId[0] : frozenIds.toArray(new NutsId[0]);
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsPushCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsPushCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public NutsPushCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsPushCommand frozenId(NutsId id) {
        return addFrozenId(id);
    }

    @Override
    public NutsPushCommand frozenId(String id) {
        return addFrozenId(id);
    }

    @Override
    public NutsPushCommand frozenIds(NutsId... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsPushCommand frozenIds(String... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsPushCommand arg(String arg) {
        return addArg(arg);
    }

    @Override
    public NutsPushCommand args(String... args) {
        return addArgs(args);
    }

    @Override
    public NutsPushCommand args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NutsPushCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsPushCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsPushCommand clearFrozenIds() {
        frozenIds = null;
        return this;
    }

    @Override
    public NutsPushCommand offline() {
        return offline(true);
    }

    @Override
    public NutsPushCommand offline(boolean enable) {
        return setOffline(enable);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "-o":
            case "--offline": {
                setOffline(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-x":
            case "--freeze": {
                for (String id : cmdLine.nextString().getStringValue().split(",")) {
                    frozenId(id);
                }
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                setRepository(cmdLine.nextString().getStringValue());
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
                    cmdLine.unexpectedArgument();
                } else {
                    cmdLine.skip();
                    id(a.getString());
                    return true;
                }
            }
        }
        return false;
    }
}
