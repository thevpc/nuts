package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;

public class GradleFirstNDependencySolver extends MavenNDependencySolver {
    public GradleFirstNDependencySolver(NSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
