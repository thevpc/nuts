package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NComparator;

public class NComparatorWithDescription<T> implements NComparator<T> {
    private NComparator<T> comparator;
    private NEDesc description;

    public NComparatorWithDescription(NComparator<T> comparator, NEDesc description) {
        this.comparator = comparator;
        this.description = description;
    }

    @Override
    public NComparator<T> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribeOfBase(session, description, comparator);
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

}
