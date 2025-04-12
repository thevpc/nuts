package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NId;

import java.util.LinkedHashMap;
import java.util.Map;

class NDependencyInfoSet {

    Map<NId, NDependencyInfo> visitedSet = new LinkedHashMap<>();

    public boolean isVisited(NDependency other) {
        return visitedSet.containsKey(NDependencyInfo.normalizedId(other));
    }

    public NDependencyInfo find(NDependency other) {
        return visitedSet.get(NDependencyInfo.normalizedId(other));
    }

    public boolean contains(NDependencyInfo newDep) {
        NDependencyInfo oldDep = visitedSet.get(newDep.normalized);
        if (oldDep == null) {
            return false;
        }
        if (isBetter(newDep, oldDep)) {
            return false;
        }
        return true;
    }

    private boolean isBetter(NDependencyInfo newDep, NDependencyInfo oldDep) {
        if (oldDep == null) {
            return true;
        }
        if (oldDep.provided && !newDep.provided) {
            return true;
        }
//            if (oldDep.depth == newDep.depth) {
//                return false;
////                if (newDep.real.getVersion().compareTo(oldDep.real.getVersion()) > 0) {
////                    return false;
////                }
//            }
        return oldDep.depth > newDep.depth;
    }

    public boolean add(NDependencyInfo other) {
        NDependencyInfo old = visitedSet.get(other.normalized);
        if (old == null) {
            visitedSet.put(other.normalized, other);
            return true;
        } else if (isBetter(other, old)) {
            visitedSet.put(other.normalized, other);
            // TODO, should we not remove sub dependencies???
            return true;
        }
        return false;
    }

}
