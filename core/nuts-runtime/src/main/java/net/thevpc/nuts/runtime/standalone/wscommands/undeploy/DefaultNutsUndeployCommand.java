package net.thevpc.nuts.runtime.standalone.wscommands.undeploy;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.undeploy.AbstractNutsUndeployCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;

public class DefaultNutsUndeployCommand extends AbstractNutsUndeployCommand {

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUndeployCommand run() {
        NutsWorkspaceUtils.of(getSession()).checkReadOnly();
        if (ids.isEmpty()) {
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("no package to undeploy"), 1);
        }
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsSession searchSession = getSession();
        for (NutsId id : ids) {
            NutsDefinition p = ws.search()
                    .setSession(searchSession
                            .copy()
                            .setFetchStrategy(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    )
                    .addIds(id)
                    .addRepositoryFilter(ws.filters().repository().byName(getRepository()))
                    //skip 'installed' repository
                    .setRepositoryFilter(
                            ws.repos().filter().byName(DefaultNutsInstalledRepository.INSTALLED_REPO_UUID).neg()
                    )
                    .setDistinct(true)
                    .setFailFast(true)
                    .getResultDefinitions().required();
            NutsRepository repository1 = ws.repos().setSession(getSession()).getRepository(p.getRepositoryUuid());
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(getSession()).repoSPI(repository1);
            repoSPI.undeploy()
                    .setId(p.getId()).setSession(getSession())
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            addResult(id);
        }
        if (getSession().isTrace()) {
            getSession().getWorkspace().formats().object(result).println();
        }
        return this;
    }

}
