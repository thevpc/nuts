package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.MavenNDependencySolver;

public class DescriptorNDependencySolver extends MavenNDependencySolver {
    public DescriptorNDependencySolver() {
        super();
    }

    @Override
    public String name() {
        return "descriptor";
    }
}
