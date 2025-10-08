package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

public class GradleFirstNDependencySolverFactory implements NDependencySolverFactory {

    public GradleFirstNDependencySolverFactory() {
    }

    @Override
    public int getScore(NScorableContext context) {
        return 1;
    }

    @Override
    public NDependencySolver create() {
        return new GradleFirstNDependencySolver();
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
