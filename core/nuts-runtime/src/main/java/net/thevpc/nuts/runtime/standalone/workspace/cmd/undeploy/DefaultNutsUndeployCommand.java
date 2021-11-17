package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
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
        NutsSession session = getSession();
        for (NutsId id : ids) {
            NutsDefinition p = getSession().search()
                    .setSession(session
                            .copy()
                            .setFetchStrategy(isOffline() ? NutsFetchStrategy.OFFLINE : NutsFetchStrategy.ONLINE)
                    )
                    .addIds(id)
                    .addRepositoryFilter(NutsRepositoryFilters.of(session).byName(getRepository()))
                    //skip 'installed' repository
                    .setRepositoryFilter(
                            session.repos().filter().byName(DefaultNutsInstalledRepository.INSTALLED_REPO_UUID).neg()
                    )
                    .setDistinct(true)
                    .setFailFast(true)
                    .getResultDefinitions().required();
            NutsRepository repository1 = session.repos().setSession(getSession()).getRepository(p.getRepositoryUuid());
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(getSession()).repoSPI(repository1);
            repoSPI.undeploy()
                    .setId(p.getId()).setSession(getSession())
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            addResult(id);
        }
        if (getSession().isTrace()) {
            getSession().out().printlnf(result);
        }
        return this;
    }

}
