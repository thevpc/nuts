package net.thevpc.nuts.runtime.standalone.solvers;

import net.thevpc.nuts.NutsSession;

public class GradleNutsDependencySolver extends MavenNutsDependencySolver{
    public GradleNutsDependencySolver(NutsSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
