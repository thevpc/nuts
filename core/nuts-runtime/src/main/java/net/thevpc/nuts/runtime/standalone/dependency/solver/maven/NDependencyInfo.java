package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NId;

class NDependencyInfo {

    NId normalized;
    NId real;
    int depth;
    NDependency dependency;
    boolean optional;
    boolean provided;

    public NDependencyInfo(NDependency dependency, int depth, boolean optional, boolean provided) {
        this.real=dependency.toId();
        this.normalized = normalizedId(real);
        this.depth = depth;
        this.dependency = dependency;
        this.optional = optional;
        this.provided = provided;
    }

    public static NId normalizedId(NId id) {
        return id.getShortId();
    }

    public static NId normalizedId(NDependency id) {
        return normalizedId(id.toId());
    }

    public static NDependencyInfo of(NDependencyTreeNodeBuild currentNode) {
        return new NDependencyInfo(currentNode.dependency, currentNode.depth, currentNode.optional, currentNode.provided);
    }

    public NDependency getDependency() {
        return dependency;
    }

    @Override
    public String toString() {
        return "NDependencyInfo{" +
                "normalized=" + normalized +
                ", real=" + real +
                ", depth=" + depth +
                ", dependency=" + dependency +
                '}';
    }
}
