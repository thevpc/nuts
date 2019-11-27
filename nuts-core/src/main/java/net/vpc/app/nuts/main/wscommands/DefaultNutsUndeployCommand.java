package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;

import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsUndeployCommand;

public class DefaultNutsUndeployCommand extends AbstractNutsUndeployCommand {

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUndeployCommand run() {
        NutsWorkspaceUtils.of(ws).checkReadOnly();

        NutsFetchCommand fetchOptions = ws.fetch().transitive(this.isTransitive());
        if (ids.isEmpty()) {
            throw new NutsExecutionException(ws, "No component to undeploy", 1);
        }
        NutsSession searchSession = CoreNutsUtils.silent(getValidSession());
        for (NutsId id : ids) {
            NutsDefinition p = ws.search()
                    .session(searchSession)
                    .ids(id)
                    .repositories(getRepository())
                    .transitive(isTransitive())
                    .fetchStrategy(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    .distinct()
                    .failFast()
                    .getResultDefinitions().required();
            NutsRepository repository1 = ws.config().getRepository(p.getRepositoryUuid(), true);
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(),
                    repository1,
                    NutsFetchMode.LOCAL, fetchOptions);
            repository1.undeploy()
                    .setId(p.getId()).setSession(rsession)
                    .run();
            addResult(id);
        }
        if (getValidSession().isTrace()) {
            ws.object().session(getValidSession()).value(result).println();
        }
        return this;
    }

}
