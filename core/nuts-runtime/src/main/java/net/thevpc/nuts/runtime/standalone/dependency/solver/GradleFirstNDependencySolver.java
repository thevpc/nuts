package net.thevpc.nuts.runtime.standalone.dependency.solver;

public class GradleFirstNDependencySolver extends MavenNDependencySolver {
    public GradleFirstNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
