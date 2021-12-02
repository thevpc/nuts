package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NutsSession;

public class MavenFirstNutsDependencySolver extends MavenNutsDependencySolver{
    public MavenFirstNutsDependencySolver(NutsSession session) {
        super(session);
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
