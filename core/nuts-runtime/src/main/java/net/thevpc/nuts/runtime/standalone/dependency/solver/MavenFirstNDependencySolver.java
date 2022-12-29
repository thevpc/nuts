package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;

public class MavenFirstNDependencySolver extends MavenNDependencySolver {
    public MavenFirstNDependencySolver(NSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
