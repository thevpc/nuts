package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.MavenNDependencySolver;

public class GradleFirstNDependencySolver extends MavenNDependencySolver {
    public GradleFirstNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
