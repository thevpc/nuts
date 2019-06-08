package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.util.ArrayList;
import java.util.List;

import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsUndeployCommand extends NutsWorkspaceCommandBase<NutsUndeployCommand> implements NutsUndeployCommand {

    private List<NutsId> result;
    private final List<NutsId> ids = new ArrayList<>();
    private String repository;
    private boolean offline = true;
    private boolean transitive = true;

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
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
        return addId(CoreStringUtils.isBlank(id) ? null : ws.parse().requiredId(id));
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
                    ids.add(ws.parse().requiredId(s));
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

    @Override
    public NutsUndeployCommand run() {
        NutsWorkspaceUtils.checkReadOnly(ws);

        NutsFetchCommand fetchOptions = ws.fetch().setTransitive(this.isTransitive());
        if (ids.isEmpty()) {
            throw new NutsExecutionException(ws, "No component to undeploy", 1);
        }
        NutsSession searchSession = getValidSession().copy().trace(false);
        for (NutsId id : ids) {
            NutsDefinition p = ws.search()
                    .session(searchSession)
                    .ids(id)
                    .repositories(getRepository())
                    .setTransitive(isTransitive())
                    .setFetchStratery(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    .duplicates(false)
                    .failFast()
                    .getResultDefinitions().required();
            NutsRepository repository1 = ws.config().getRepository(p.getRepositoryUuid(), true);
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(),
                    repository1,
                    NutsFetchMode.LOCAL, fetchOptions);
            repository1.undeploy()
                    .id(p.getId()).session(rsession)
                    .run();
            addResult(id);
        }
        if (getValidSession().isTrace()) {
            ws.format().object().session(getValidSession()).value(result).println();
        }
        return this;
    }

    private void addResult(NutsId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        if (getValidSession().isTrace()) {
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                    getValidSession().getTerminal().out().printf("Nuts %N undeployed successfully%n", ws.format().id().toString(id));
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
