package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNutsUndeployCommand extends NutsWorkspaceCommandBase<NutsUndeployCommand> implements NutsUndeployCommand {

    protected List<NutsId> result;
    protected final List<NutsId> ids = new ArrayList<>();
    protected String repository;
    protected boolean offline = true;
    protected boolean transitive = true;

    public AbstractNutsUndeployCommand(NutsWorkspace ws) {
        super(ws, "undeploy");
    }

    @Override
    public NutsId[] getIds() {
        return ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsUndeployCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsUndeployCommand id(String id) {
        return addId(id);
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
        return addId(CoreStringUtils.isBlank(id) ? null : ws.id().parseRequired(id));
    }

    @Override
    public NutsUndeployCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsUndeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parseRequired(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsUndeployCommand ids(NutsId... values) {
        return addIds(values);
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

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsUndeployCommand transitive() {
        return setTransitive(true);
    }

    @Override
    public NutsUndeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }


    protected void addResult(NutsId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        if (getValidSession().isTrace()) {
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                    getValidSession().getTerminal().out().printf("Nuts %s undeployed successfully%n", new NutsString(ws.id().value(id).format()));
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
    public NutsUndeployCommand offline() {
        return offline(true);
    }

    @Override
    public NutsUndeployCommand offline(boolean offline) {
        return setOffline(offline);
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
        switch (a.getStringKey()) {
            case "--offline": {
                setOffline(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                setRepository(cmdLine.nextString().getStringValue());
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
                    id(a.getString());
                    return true;
                }
            }
        }
        return false;
    }

}
