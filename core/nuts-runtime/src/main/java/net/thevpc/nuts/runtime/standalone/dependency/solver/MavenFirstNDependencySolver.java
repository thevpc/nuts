package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.MavenNDependencySolver;

public class MavenFirstNDependencySolver extends MavenNDependencySolver {
    public MavenFirstNDependencySolver() {
        super();
    }

    @Override
    public String getName() {
        return "maven-first";
    }
}
