package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

public class DescriptorNDependencySolverFactory implements NDependencySolverFactory {

    public DescriptorNDependencySolverFactory() {
    }

    @Override
    public int getScore(NScorableContext context) {
        return 1;
    }

    @Override
    public NDependencySolver create() {
        return new DescriptorNDependencySolver();
    }

    @Override
    public String getName() {
        return "descriptor";
    }
}
