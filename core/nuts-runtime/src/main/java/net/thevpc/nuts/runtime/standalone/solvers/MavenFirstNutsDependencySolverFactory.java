package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsDependencySolverFactory;

public class MavenFirstNutsDependencySolverFactory implements NutsDependencySolverFactory {
    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return 1;
    }

    @Override
    public NutsDependencySolver create(NutsSession session) {
        return new MavenFirstNutsDependencySolver(session);
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
