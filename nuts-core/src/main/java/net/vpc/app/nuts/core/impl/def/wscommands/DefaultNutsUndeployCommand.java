package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.*;

import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.wscommands.AbstractNutsUndeployCommand;

public class DefaultNutsUndeployCommand extends AbstractNutsUndeployCommand {

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
        super(ws);
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
