package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

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
        NutsSession searchSession = CoreNutsUtils.silent(getValidWorkspaceSession());
        for (NutsId id : ids) {
            NutsDefinition p = ws.search()
                    .setSession(searchSession)
                    .addIds(id)
                    .addRepositories(getRepository())
                    .setTransitive(isTransitive())
                    .setFetchStrategy(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    //skip 'installed' repository
                    .setRepositoryFilter(
                                    ws.repos().filter().byName(DefaultNutsInstalledRepository.INSTALLED_REPO_UUID).neg()
                    )
                    .setDistinct(true)
                    .setFailFast(true)
                    .getResultDefinitions().required();
            NutsRepository repository1 = ws.repos().getRepository(p.getRepositoryUuid(), session);
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repository1);
            repoSPI.undeploy()
                    .setId(p.getId()).setSession(getValidWorkspaceSession())
//                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            addResult(id);
        }
        if (getValidWorkspaceSession().isTrace()) {
            getValidWorkspaceSession().formatObject(result).println();
        }
        return this;
    }

}
