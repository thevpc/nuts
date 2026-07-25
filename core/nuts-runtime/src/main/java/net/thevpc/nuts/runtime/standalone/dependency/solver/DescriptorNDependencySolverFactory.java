package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.artifact.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DescriptorNDependencySolverFactory implements NDependencySolverFactory {

    public DescriptorNDependencySolverFactory() {
    }

    @Override
    public NDependencySolver create() {
        return new DescriptorNDependencySolver();
    }

    @Override
    public String name() {
        return "descriptor";
    }
}
