/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.wscommands;

import net.thevpc.nuts.*;

import java.util.*;

/**
 *
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNutsUninstallCommand extends NutsWorkspaceCommandBase<NutsUninstallCommand> implements NutsUninstallCommand {

    private boolean erase = false;
    private List<String> args;
    private final List<NutsId> ids = new ArrayList<>();

    public AbstractNutsUninstallCommand(NutsWorkspace ws) {
        super(ws, "uninstall");
    }

    @Override
    public NutsUninstallCommand addId(String id) {
        return addId(id == null ? null : ws.id().parser().parse(id));
    }

    @Override
    public NutsUninstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
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
        return removeId(ws.id().parser().parse(id));
    }

    @Override
    public NutsUninstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsUninstallCommand clearArgs() {
        this.args = null;
        return this;
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
    public boolean isErase() {
        return erase;
    }

    @Override
    public NutsUninstallCommand setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled=a.isEnabled();
        switch (a.getStringKey()) {
            case "-e":
            case "--erase": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setErase(val);
                }
                return true;
            }
            case "-g":
            case "--args": {
                while (cmdLine.hasNext()) {
                    String val = cmdLine.nextString().getStringValue();
                    if (enabled) {
                        this.addArg(val);
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
                    addId(a.getString());
                    return true;
                }
            }
        }
    }

}
