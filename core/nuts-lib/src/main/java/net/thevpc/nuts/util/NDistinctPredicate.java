package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.AbstractNPredicate;

import java.util.HashSet;

class NDistinctPredicate<T> extends AbstractNPredicate<T> {
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
    public NElement describe() {
        return NElement.ofString("distinct");
    }

}
