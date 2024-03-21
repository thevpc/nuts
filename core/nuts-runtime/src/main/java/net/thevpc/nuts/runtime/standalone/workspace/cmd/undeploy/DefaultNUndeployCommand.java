package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NMsg;

public class DefaultNUndeployCommand extends AbstractNUndeployCommand {

    public DefaultNUndeployCommand(NSession session) {
        super(session);
    }

    @Override
    public NUndeployCommand run() {
        NWorkspaceUtils.of(getSession()).checkReadOnly();
        if (ids.isEmpty()) {
            throw new NExecutionException(getSession(), NMsg.ofPlain("no package to undeploy"), NExecutionException.ERROR_1);
        }
        checkSession();
        NSession session = getSession();
        for (NId id : ids) {
            NDefinition p = NSearchCommand.of(session
                            .copy()
                            .setFetchStrategy(isOffline() ? NFetchStrategy.OFFLINE : NFetchStrategy.ONLINE))
                    .addIds(id)
                    .addRepositoryFilter(NRepositoryFilters.of(session).byName(getRepository()))
                    //skip 'installed' repository
                    .setRepositoryFilter(
                            NRepositories.of(session).filter().installedRepo().neg()
                    )
                    .setDistinct(true)
                    .failFast()
                    .getResultDefinitions().findFirst().get();
            NRepository repository1 = NRepositories.of(session)
                    .findRepository(p.getRepositoryUuid()).get();
            NRepositorySPI repoSPI = NWorkspaceUtils.of(getSession()).repoSPI(repository1);
            repoSPI.undeploy()
                    .setId(p.getId()).setSession(getSession())
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            addResult(id);
        }
        if (getSession().isTrace()) {
            getSession().out().println(result);
        }
        return this;
    }

}
