package net.thevpc.nuts.runtime.standalone.wscommands.undeploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsWorkspaceCommandBase;

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
    public NutsId[] getIds() {
        return ids.toArray(new NutsId[0]);
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
        NutsWorkspace ws = getSession().getWorkspace();
        return addId(NutsUtilStrings.isBlank(id) ? null : ws.id().parser().setLenient(false).parse(id));
    }

    @Override
    public NutsUndeployCommand addIds(String... values) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NutsUtilStrings.isBlank(s)) {
                    ids.add(ws.id().parser().setLenient(false).parse(s));
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
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--offline": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOffline(val);
                }
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                String val = cmdLine.nextString().getStringValue();
                if (enabled) {
                    setRepository(val);
                }
                break;
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
        return false;
    }

}
