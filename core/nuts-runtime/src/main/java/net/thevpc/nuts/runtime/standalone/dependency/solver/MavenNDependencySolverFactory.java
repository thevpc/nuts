package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.MavenNDependencySolver;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class MavenNDependencySolverFactory implements NDependencySolverFactory {

    public MavenNDependencySolverFactory() {
    }

    @Override
    public NDependencySolver create() {
        return new MavenNDependencySolver();
    }

    @Override
    public String getName() {
        return "maven";
    }
}
