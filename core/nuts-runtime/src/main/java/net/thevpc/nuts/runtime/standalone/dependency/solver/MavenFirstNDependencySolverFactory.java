package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

public class MavenFirstNDependencySolverFactory implements NDependencySolverFactory {
    public MavenFirstNDependencySolverFactory() {
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return 1;
    }

    @Override
    public NDependencySolver create() {
        return new MavenFirstNDependencySolver();
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
