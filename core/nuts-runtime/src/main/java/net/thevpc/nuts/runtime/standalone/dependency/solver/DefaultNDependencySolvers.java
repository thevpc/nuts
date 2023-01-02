package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.spi.NDependencySolvers;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.List;

public class DefaultNDependencySolvers implements NDependencySolvers {
    private NWorkspace ws;
    public DefaultNDependencySolvers(NWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public NDependencySolver createSolver(NSession session) {
        NSessionUtils.checkSession(ws, session);
        return createSolver(session.getDependencySolver(),session);
    }

    @Override
    public NDependencySolver createSolver(String solverName, NSession session) {
        NSessionUtils.checkSession(ws, session);
        DefaultNConfigs config = (DefaultNConfigs) NConfigs.of(session);
        return config.createDependencySolver(solverName);
    }

    @Override
    public List<String> getSolverNames(NSession session) {
        NSessionUtils.checkSession(ws, session);
        DefaultNConfigs config = (DefaultNConfigs)NConfigs.of(session);
        return config.getDependencySolverNames();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
