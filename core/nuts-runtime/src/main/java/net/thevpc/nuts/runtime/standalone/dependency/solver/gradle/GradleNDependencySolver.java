package net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;

import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.MavenNDependencySolver;

public class GradleNDependencySolver extends MavenNDependencySolver {
    public GradleNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
