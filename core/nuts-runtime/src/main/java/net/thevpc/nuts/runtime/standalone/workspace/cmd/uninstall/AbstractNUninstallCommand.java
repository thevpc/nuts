/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNUninstallCommand extends NWorkspaceCommandBase<NUninstallCommand> implements NUninstallCommand {

    private boolean erase = false;
    private List<String> args;
    private final List<NId> ids = new ArrayList<>();

    public AbstractNUninstallCommand(NSession ws) {
        super(ws, "uninstall");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NUninstallCommand addId(String id) {
        checkSession();
        NSession session = getSession();
        return addId(id == null ? null : NId.of(id).get(session));
    }

    @Override
    public NUninstallCommand addId(NId id) {
        if (id == null) {
            checkSession();
            throw new NNotFoundException(getSession(), id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NUninstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstallCommand addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstallCommand removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NUninstallCommand removeId(String id) {
        checkSession();
        NSession session = getSession();
        return removeId(NId.of(id).get(session));
    }

    @Override
    public NUninstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NUninstallCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NUninstallCommand addArg(String arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NUninstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NUninstallCommand addArgs(Collection<String> args) {
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
    public List<NId> getIds() {
        return CoreCollectionUtils.unmodifiableList(ids);
    }

    @Override
    public boolean isErase() {
        return erase;
    }

    @Override
    public NUninstallCommand setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument aa = cmdLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "-e":
            case "--erase": {
                cmdLine.withNextBoolean((v, a, s) -> this.setErase(v));
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.withNextBoolean((v, a, s) -> {
                    this.addArgs(cmdLine.toStringArray());
                    cmdLine.skipAll();
                });
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
