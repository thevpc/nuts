/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.lib.common.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
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
public abstract class AbstractNUninstallCmd extends NWorkspaceCmdBase<NUninstallCmd> implements NUninstallCmd {

    private boolean erase = false;
    private List<String> args;
    private final List<NId> ids = new ArrayList<>();

    public AbstractNUninstallCmd(NSession session) {
        super(session, "uninstall");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NUninstallCmd addId(String id) {
        checkSession();
        NSession session = getSession();
        return addId(id == null ? null : NId.of(id).get(session));
    }

    @Override
    public NUninstallCmd addId(NId id) {
        if (id == null) {
            checkSession();
            throw new NNotFoundException(getSession(), id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NUninstallCmd addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstallCmd addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstallCmd removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NUninstallCmd removeId(String id) {
        checkSession();
        NSession session = getSession();
        return removeId(NId.of(id).get(session));
    }

    @Override
    public NUninstallCmd clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NUninstallCmd clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NUninstallCmd addArg(String arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NUninstallCmd addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NUninstallCmd addArgs(Collection<String> args) {
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
    public NUninstallCmd setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "-e":
            case "--erase": {
                cmdLine.withNextFlag((v, a, s) -> this.setErase(v));
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.withNextFlag((v, a, s) -> {
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
