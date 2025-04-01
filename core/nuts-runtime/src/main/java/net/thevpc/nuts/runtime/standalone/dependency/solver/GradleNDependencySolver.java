package net.thevpc.nuts.runtime.standalone.dependency.solver;

public class GradleNDependencySolver extends MavenNDependencySolver {
    public GradleNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
