package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsDependencySolverFactory;

public class MavenNutsDependencySolverFactory implements NutsDependencySolverFactory {
    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return 1;
    }

    @Override
    public NutsDependencySolver create(NutsSession session) {
        return new MavenNutsDependencySolver(session);
    }

    @Override
    public String getName() {
        return "maven";
    }
}
