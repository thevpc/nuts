package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;

public class GradleFirstNutsDependencySolver extends MavenNutsDependencySolver{
    public GradleFirstNutsDependencySolver(NutsSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
