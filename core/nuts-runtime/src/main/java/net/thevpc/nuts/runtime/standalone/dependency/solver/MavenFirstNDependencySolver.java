package net.thevpc.nuts.runtime.standalone.dependency.solver;

public class MavenFirstNDependencySolver extends MavenNDependencySolver {
    public MavenFirstNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
