package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

public class MavenFirstNDependencySolverFactory implements NDependencySolverFactory {
    public MavenFirstNDependencySolverFactory() {
    }

    @Override
    public int getScore(NScorableContext context) {
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
