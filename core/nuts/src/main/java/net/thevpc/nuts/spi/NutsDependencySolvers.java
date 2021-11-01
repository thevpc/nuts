package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsDependencySolvers extends NutsComponent<Object> {
    static NutsDependencySolvers of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsDependencySolvers.class,true,null);
    }

    NutsDependencySolver createSolver(NutsSession session);

    NutsDependencySolver createSolver(String solverName,NutsSession session);

    String[] getSolverNames(NutsSession session);

}
