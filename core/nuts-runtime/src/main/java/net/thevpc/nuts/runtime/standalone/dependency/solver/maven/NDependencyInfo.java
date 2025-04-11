package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NId;

class NDependencyInfo {

    NId normalized;
    NId real;
    int depth;
    NDependency effDependency;
    boolean optional;
    boolean provided;

    public NDependencyInfo(NId normalized, NId real, NDependency dependency, int depth, boolean optional, boolean provided) {
        this.normalized = normalized;
        this.real = real;
        this.depth = depth;
        this.effDependency = dependency;
        this.optional = optional;
        this.provided = provided;
    }

    public static NDependencyInfo of(NDependencyTreeNodeBuild currentNode) {
        NId id = currentNode.def == null ? null : currentNode.def.getId();
        if (id == null) {
            id = currentNode.effDependency.toId();
        }
        return new NDependencyInfo(id.getShortId(), id, currentNode.effDependency, currentNode.depth, currentNode.optional, currentNode.provided);
    }

    public NDependency getDependency() {
        return effDependency;
    }

    @Override
    public String toString() {
        return "NDependencyInfo{" +
                "normalized=" + normalized +
                ", real=" + real +
                ", depth=" + depth +
                ", effDependency=" + effDependency +
                '}';
    }
}
