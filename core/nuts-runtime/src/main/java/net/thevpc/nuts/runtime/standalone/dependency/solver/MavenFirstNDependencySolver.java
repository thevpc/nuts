package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

public class MavenFirstNDependencySolver extends MavenNDependencySolver {
    public MavenFirstNDependencySolver(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
