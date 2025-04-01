package net.thevpc.nuts.runtime.standalone.dependency.solver;

public class DescriptorNDependencySolver extends MavenNDependencySolver {
    public DescriptorNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "descriptor";
    }
}
