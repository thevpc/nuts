package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.util.NutsDescribable;
import net.thevpc.nuts.util.NutsPredicates;

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
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofString("distinct");
    }

}
