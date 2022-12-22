package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNutsUndeployCommand extends NutsWorkspaceCommandBase<NutsUndeployCommand> implements NutsUndeployCommand {

    protected List<NutsId> result;
    protected final List<NutsId> ids = new ArrayList<>();
    protected String repository;
    protected boolean offline = true;

    public AbstractNutsUndeployCommand(NutsWorkspace ws) {
        super(ws, "undeploy");
    }

    @Override
    public List<NutsId> getIds() {
        return ids;
    }

    @Override
    public NutsUndeployCommand addId(NutsId id) {
        if (id != null) {
            ids.add(id);
        }
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand addId(String id) {
        checkSession();
        NutsSession session = getSession();
        return addId(NutsBlankable.isBlank(id) ? null : NutsId.of(id).get(session));
    }

    @Override
    public NutsUndeployCommand addIds(String... values) {
        checkSession();
        NutsSession session = getSession();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(NutsId.of(s).get(session));
                }
            }
        }
        return this;
    }

    @Override
    public NutsUndeployCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsUndeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsUndeployCommand setRepository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    protected void addResult(NutsId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        checkSession();
        NutsSession session = getSession();
        if (session.isTrace()) {
            if (session.getOutputFormat() == null || session.getOutputFormat() == NutsContentType.PLAIN) {
                if (session.getOutputFormat() == null || session.getOutputFormat() == NutsContentType.PLAIN) {
                    session.getTerminal().out().printf("Nuts %s undeployed successfully%n", id);
                }
            }
        }
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument aa = cmdLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch(aa.key()) {
            case "--offline": {
                cmdLine.withNextBoolean((v,a,s)-> setOffline(v));
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                cmdLine.withNextString((v,a,s)-> setRepository(v));
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
