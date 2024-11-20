package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NWorkspace;

public class DescriptorNDependencySolver extends MavenNDependencySolver {
    public DescriptorNDependencySolver(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public String getName() {
        return "descriptor";
    }
}
