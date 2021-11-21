package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsPredicates;
import net.thevpc.nuts.NutsSession;

import java.util.HashSet;

class DistinctPredicate<T> extends NutsPredicates.BasePredicate<T> implements IterInfoNodeAware {
    private HashSet<T> visited = new HashSet<>();

    @Override
    public boolean test(T value) {
        if (visited.contains(value)) {
            return false;
        }
        visited.add(value);
        return true;
    }

    @Override
    public String toString() {
        return "Distinct";
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return IterInfoNode.ofLiteralType("Predicate","distinct",null,null);
    }
}
