package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNUndeployCmd extends NWorkspaceCmdBase<NUndeployCmd> implements NUndeployCmd {

    protected List<NId> result;
    protected final List<NId> ids = new ArrayList<>();
    protected String repository;
    protected boolean offline = true;

    public AbstractNUndeployCmd(NWorkspace workspace) {
        super(workspace, "undeploy");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public List<NId> getIds() {
        return ids;
    }

    @Override
    public NUndeployCmd addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        invalidateResult();
        return this;
    }

    @Override
    public NUndeployCmd addId(String id) {
        return addId(NBlankable.isBlank(id) ? null : NId.get(id).get());
    }

    @Override
    public NUndeployCmd addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.get(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NUndeployCmd addIds(NId... value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NUndeployCmd clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NUndeployCmd setRepository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    protected void addResult(NId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        NSession session=workspace.currentSession();
        if (session.isTrace()) {
            if (session.getOutputFormat().orNull() == null || session.getOutputFormat().orDefault() == NContentType.PLAIN) {
                if (session.getOutputFormat().orNull() == null || session.getOutputFormat().orDefault() == NContentType.PLAIN) {
                    session.getTerminal().out().println(NMsg.ofC("Nuts %s undeployed successfully", id));
                }
            }
        }
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NUndeployCmd setOffline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "--offline": {
                cmdLine.withNextFlag((v, a) -> setOffline(v));
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                cmdLine.withNextEntry((v, a) -> setRepository(v));
                break;
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
        return false;
    }

}
