package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class GradleFirstNDependencySolverFactory implements NDependencySolverFactory {

    public GradleFirstNDependencySolverFactory() {
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
