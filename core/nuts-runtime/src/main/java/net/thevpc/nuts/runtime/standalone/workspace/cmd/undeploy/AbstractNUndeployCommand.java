package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNUndeployCommand extends NWorkspaceCommandBase<NUndeployCommand> implements NUndeployCommand {

    protected List<NId> result;
    protected final List<NId> ids = new ArrayList<>();
    protected String repository;
    protected boolean offline = true;

    public AbstractNUndeployCommand(NSession session) {
        super(session, "undeploy");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }

    @Override
    public List<NId> getIds() {
        return ids;
    }

    @Override
    public NUndeployCommand addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        invalidateResult();
        return this;
    }

    @Override
    public NUndeployCommand addId(String id) {
        checkSession();
        NSession session = getSession();
        return addId(NBlankable.isBlank(id) ? null : NId.of(id).get(session));
    }

    @Override
    public NUndeployCommand addIds(String... values) {
        checkSession();
        NSession session = getSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.of(s).get(session));
                }
            }
        }
        return this;
    }

    @Override
    public NUndeployCommand addIds(NId... value) {
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
    public NUndeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NUndeployCommand setRepository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    protected void addResult(NId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        checkSession();
        NSession session = getSession();
        if (session.isTrace()) {
            if (session.getOutputFormat() == null || session.getOutputFormat() == NContentType.PLAIN) {
                if (session.getOutputFormat() == null || session.getOutputFormat() == NContentType.PLAIN) {
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
    public NUndeployCommand setOffline(boolean offline) {
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
                cmdLine.withNextFlag((v, a, s) -> setOffline(v));
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                cmdLine.withNextEntry((v, a, s) -> setRepository(v));
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
                    addId(aa.asString().get(session));
                    return true;
                }
            }
        }
        return false;
    }

}
