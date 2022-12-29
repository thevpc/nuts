package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;

public class DescriptorNDependencySolver extends MavenNDependencySolver {
    public DescriptorNDependencySolver(NSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "descriptor";
    }
}
