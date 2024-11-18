package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

public class GradleNDependencySolver extends MavenNDependencySolver {
    public GradleNDependencySolver(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
