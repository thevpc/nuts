package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;

public class DescriptorNutsDependencySolver extends MavenNutsDependencySolver{
    public DescriptorNutsDependencySolver(NutsSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "descriptor";
    }
}
