package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.gradle.GradleNDependencySolver;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.artifact.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class GradleNDependencySolverFactory implements NDependencySolverFactory {

    public GradleNDependencySolverFactory() {
    }

    @Override
    public NDependencySolver create() {
        return new GradleNDependencySolver();
    }

    @Override
    public String name() {
        return "gradle";
    }
}
