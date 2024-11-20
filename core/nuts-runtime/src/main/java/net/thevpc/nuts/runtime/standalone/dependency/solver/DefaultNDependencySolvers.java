package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.spi.NDependencySolvers;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.List;

public class DefaultNDependencySolvers implements NDependencySolvers {
    private NWorkspace ws;
    public DefaultNDependencySolvers(NWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public NDependencySolver createSolver() {
        NSession session = NSession.of().get();
        return createSolver(session.getDependencySolver());
    }

    @Override
    public NDependencySolver createSolver(String solverName) {
        DefaultNConfigs config = (DefaultNConfigs) NConfigs.of();
        return config.createDependencySolver(solverName);
    }

    @Override
    public List<String> getSolverNames() {
        DefaultNConfigs config = (DefaultNConfigs)NConfigs.of();
        return config.getDependencySolverNames();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
