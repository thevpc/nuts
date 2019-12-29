package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;

import net.vpc.app.nuts.main.repos.DefaultNutsInstalledRepository;
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
        if (ids.isEmpty()) {
            throw new NutsExecutionException(ws, "No component to undeploy", 1);
        }
        NutsSession searchSession = CoreNutsUtils.silent(getSession());
        for (NutsId id : ids) {
            NutsDefinition p = ws.search()
                    .session(searchSession)
                    .ids(id)
                    .repositories(getRepository())
                    .transitive(isTransitive())
                    .fetchStrategy(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    //skip 'installed' repository
                    .repositoryFilter(repository -> ! DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(repository.getUuid()))
                    .distinct()
                    .failFast()
                    .getResultDefinitions().required();
            NutsRepository repository1 = ws.config().getRepository(p.getRepositoryUuid(), true);
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getSession(),
                    repository1,
                    NutsFetchMode.LOCAL);
            repository1.undeploy()
                    .setId(p.getId()).setSession(rsession)
                    .run();
            addResult(id);
        }
        if (getSession().isTrace()) {
            getSession().formatObject(result).println();
        }
        return this;
    }

}
