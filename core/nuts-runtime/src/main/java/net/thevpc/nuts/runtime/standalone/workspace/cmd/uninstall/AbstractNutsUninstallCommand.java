/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
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
        checkSession();
        NutsSession session = getSession();
        return addId(id == null ? null : NutsId.of(id).get(session));
    }

    @Override
    public NutsUninstallCommand addId(NutsId id) {
        if (id == null) {
            checkSession();
            throw new NutsNotFoundException(getSession(), id);
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
        checkSession();
        NutsSession session = getSession();
        return removeId(NutsId.of(id).get(session));
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
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
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
    public List<NutsId> getIds() {
        return CoreCollectionUtils.unmodifiableList(ids);
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
        NutsArgument aa = cmdLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "-e":
            case "--erase": {
                cmdLine.withNextBoolean((v, a, s) -> this.setErase(v), session);
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.withNextBoolean((v, a, s) -> {
                    this.addArgs(cmdLine.toStringArray());
                    cmdLine.skipAll();
                }, session);
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (aa.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(aa.asString().get(session));
                    return true;
                }
            }
        }
    }

}
