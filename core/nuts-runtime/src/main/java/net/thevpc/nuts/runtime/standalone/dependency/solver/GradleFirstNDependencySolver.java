package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NWorkspace;

public class GradleFirstNDependencySolver extends MavenNDependencySolver {
    public GradleFirstNDependencySolver(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
