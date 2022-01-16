package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
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
        NutsSessionUtils.checkSession(ws, session);
        return createSolver(session.getDependencySolver(),session);
    }

    @Override
    public NutsDependencySolver createSolver(String solverName,NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager)session.config();
        return config.createDependencySolver(solverName);
    }

    @Override
    public String[] getSolverNames(NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager)session.config();
        return config.getDependencySolverNames();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
