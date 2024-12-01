package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NMsg;

public class DefaultNUndeployCmd extends AbstractNUndeployCmd {

    public DefaultNUndeployCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public NUndeployCmd run() {
        NSession session=workspace.currentSession();
        NWorkspaceUtils.of(workspace).checkReadOnly();
        if (ids.isEmpty()) {
            throw new NExecutionException(NMsg.ofPlain("no package to undeploy"), NExecutionException.ERROR_1);
        }
        for (NId id : ids) {
            NDefinition p = NSearchCmd.of()
                    .setFetchStrategy(isOffline() ? NFetchStrategy.OFFLINE : NFetchStrategy.ONLINE)
                    .addIds(id)
                    .addRepositoryFilter(NRepositoryFilters.of().byName(getRepository()))
                    //skip 'installed' repository
                    .setRepositoryFilter(
                            NRepositoryFilters.of().installedRepo().neg()
                    )
                    .setDistinct(true)
                    .failFast()
                    .getResultDefinitions().findFirst().get();
            NRepository repository1 = workspace
                    .findRepository(p.getRepositoryUuid()).get();
            NRepositorySPI repoSPI = NWorkspaceUtils.of(workspace).repoSPI(repository1);
            repoSPI.undeploy()
                    .setId(p.getId())
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            addResult(id);
        }
        if (session.isTrace()) {
            session.out().println(result);
        }
        return this;
    }

}
