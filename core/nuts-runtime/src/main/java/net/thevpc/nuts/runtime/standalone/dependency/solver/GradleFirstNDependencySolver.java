package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.gradle.GradleNDependencySolver;

public class GradleFirstNDependencySolver extends GradleNDependencySolver {
    public GradleFirstNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
