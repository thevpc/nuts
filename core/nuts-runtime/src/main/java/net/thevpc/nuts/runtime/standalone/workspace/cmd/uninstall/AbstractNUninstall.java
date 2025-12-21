/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NUninstall;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.AbstractNInstall;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNUninstall extends NWorkspaceCmdBase<NUninstall> implements NUninstall {

    protected boolean erase = false;
    protected List<String> args;
    protected List<AbstractNInstall.ConditionalArguments> conditionalArguments = new ArrayList<>();
    protected final List<NId> ids = new ArrayList<>();

    public AbstractNUninstall() {
        super("uninstall");
    }

    @Override
    public NUninstall addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NUninstall addId(NId id) {
        if (id == null) {
            throw new NArtifactNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NUninstall addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstall addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NUninstall removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NUninstall removeId(String id) {
        return removeId(NId.get(id).get());
    }

    @Override
    public NUninstall clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NUninstall clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return NCollections.unmodifiableList(args);
    }

    @Override
    public NUninstall addArg(String arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NUninstall addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NUninstall addArgs(Collection<String> args) {
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
        return NCollections.unmodifiableList(ids);
    }

    @Override
    public boolean isErase() {
        return erase;
    }

    @Override
    public NUninstall setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "-e":
            case "--erase": {
                cmdLine.matcher().matchFlag((v) -> this.setErase(v.booleanValue())).anyMatch();
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.matcher().matchFlag((v) -> {
                    this.addArgs(cmdLine.toStringArray());
                    cmdLine.skipAll();
                }).anyMatch();
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
                    addId(aa.asString().get());
                    return true;
                }
            }
        }
    }

}
