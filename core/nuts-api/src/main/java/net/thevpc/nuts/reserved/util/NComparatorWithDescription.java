package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NComparator;

import java.util.function.Supplier;

public class NComparatorWithDescription<T> implements NComparator<T> {
    private NComparator<T> comparator;
    private Supplier<NElement> description;

    public NComparatorWithDescription(NComparator<T> comparator, Supplier<NElement> description) {
        this.comparator = comparator;
        this.description = description;
    }

    @Override
    public NComparator<T> redescribe(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, comparator);
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

}
