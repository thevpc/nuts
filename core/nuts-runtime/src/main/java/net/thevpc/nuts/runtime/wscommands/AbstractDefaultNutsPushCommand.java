/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.wscommands;

import net.thevpc.nuts.*;

import java.util.*;

/**
 *
 * @author thevpc
 */
public abstract class AbstractDefaultNutsPushCommand extends NutsWorkspaceCommandBase<NutsPushCommand> implements NutsPushCommand {

    protected boolean offline = false;
    protected List<String> args;
    protected final List<NutsId> ids = new ArrayList<>();
    protected List<NutsId> lockedIds;
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
        return addId(id == null ? null : ws.id().parser().setLenient(false).parse(id));
    }

    @Override
    public NutsPushCommand addLockedId(String id) {
        return addLockedId(id == null ? null : ws.id().parser().setLenient(false).parse(id));
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
            ids.remove(ws.id().parser().parse(id));
        }
        return this;
    }

    @Override
    public NutsPushCommand removeLockedId(NutsId id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(id);
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand removeLockedId(String id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(ws.id().parser().parse(id));
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand addLockedId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
        } else {
            if (lockedIds == null) {
                lockedIds = new ArrayList<>();
            }
            lockedIds.add(id);
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
    public NutsPushCommand addLockedIds(String... values) {
        for (String id : values) {
            addLockedId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addLockedIds(NutsId... values) {
        for (NutsId id : values) {
            addLockedId(id);
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
    public NutsId[] getLockedIds() {
        return lockedIds == null ? new NutsId[0] : lockedIds.toArray(new NutsId[0]);
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
    public NutsPushCommand lockedId(NutsId id) {
        return addLockedId(id);
    }

    @Override
    public NutsPushCommand lockedId(String id) {
        return addLockedId(id);
    }

    @Override
    public NutsPushCommand lockedIds(NutsId... values) {
        return addLockedIds(values);
    }

    @Override
    public NutsPushCommand lockedIds(String... values) {
        return addLockedIds(values);
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
    public NutsPushCommand clearLockedIds() {
        lockedIds = null;
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
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "-o":
            case "--offline": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOffline(val);
                }
                return true;
            }
            case "-x":
            case "--freeze": {
                for (String id : cmdLine.nextString().getStringValue().split(",")) {
                    if (enabled) {
                        lockedId(id);
                    }
                }
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    setRepository(val);
                }
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                if(enabled) {
                    this.addArgs(cmdLine.toStringArray());
                }
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
