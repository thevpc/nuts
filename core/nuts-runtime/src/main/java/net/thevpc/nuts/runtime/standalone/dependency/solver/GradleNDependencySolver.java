package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;

public class GradleNDependencySolver extends MavenNDependencySolver {
    public GradleNDependencySolver(NSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
