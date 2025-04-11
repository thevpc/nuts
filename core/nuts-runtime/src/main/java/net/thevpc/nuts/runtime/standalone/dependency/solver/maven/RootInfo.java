package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDependency;

class RootInfo {
    NDependency dependency;
    NDefinition def;

    public RootInfo(NDependency dependency, NDefinition def) {
        this.dependency = dependency;
        this.def = def;
    }
}
