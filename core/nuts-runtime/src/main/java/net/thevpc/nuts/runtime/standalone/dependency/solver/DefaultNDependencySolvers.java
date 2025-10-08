package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NDependencySolvers;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NScorableContext;

import java.util.List;

public class DefaultNDependencySolvers implements NDependencySolvers {
    public DefaultNDependencySolvers() {
    }

    @Override
    public NDependencySolver createSolver() {
        NSession session = NSession.of();
        return createSolver(session.getDependencySolver());
    }

    @Override
    public NDependencySolver createSolver(String solverName) {
        return NWorkspaceExt.of().createDependencySolver(solverName);
    }

    @Override
    public List<String> getSolverNames() {
        return NWorkspaceExt.of().getDependencySolverNames();
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
