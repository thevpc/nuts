package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsDependencySolvers;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsDependencySolvers implements NutsDependencySolvers {
    private NutsWorkspace ws;
    public DefaultNutsDependencySolvers(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public NutsDependencySolver createSolver(NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        return createSolver(session.getDependencySolver(),session);
    }

    @Override
    public NutsDependencySolver createSolver(String solverName,NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager)session.config();
        return config.createDependencySolver(solverName);
    }

    @Override
    public String[] getSolverNames(NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager)session.config();
        return config.getDependencySolverNames();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}
