package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsPredicates;
import net.thevpc.nuts.NutsDescribable;

import java.util.HashSet;

class DistinctPredicate<T> extends NutsPredicates.BasePredicate<T> implements NutsDescribable {
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
    public NutsElement describe(NutsElements elems) {
        return elems.ofString("distinct");
    }

}
