package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.util.NComparator;

import java.util.Comparator;

public class NComparatorFromJavaComparator<T> implements NComparator<T> {
    private Comparator<T> comparator;

    public NComparatorFromJavaComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

}
