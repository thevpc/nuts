package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.artifact.NDependencySolvers;
import net.thevpc.nuts.artifact.NDependencySolver;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;

import java.util.List;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNDependencySolvers implements NDependencySolvers {
    public DefaultNDependencySolvers() {
    }

    @Override
    public NDependencySolver createSolver() {
        NSession session = NSession.of();
        return createSolver(session.dependencySolver());
    }

    @Override
    public NDependencySolver createSolver(String solverName) {
        return NWorkspaceExt.of().createDependencySolver(solverName);
    }

    @Override
    public List<String> solverNames() {
        return NWorkspaceExt.of().getDependencySolverNames();
    }

}
