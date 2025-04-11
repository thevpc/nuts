package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import java.util.Comparator;

class NDependencyTreeNodeBuildComparator implements Comparator<NDependencyTreeNodeBuild> {
    @Override
    public int compare(NDependencyTreeNodeBuild o1, NDependencyTreeNodeBuild o2) {
        return Integer.compare(o1.depth, o2.depth);
    }
}
