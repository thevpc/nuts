package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;
import java.util.List;

class NIteratorSorted<T> extends NIteratorBase<T> {

    private final boolean removeDuplicates;
    private final NIterator<T> it;
    private final NComparator<T> c;
    Iterator<T> base;

    public NIteratorSorted(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
        this.it = it;
        this.c = c;
        base = null;
    }

    @Override
    public NElement describe() {
        return NDescribables.describeResolveOrDestructAsObject(base)
                .builder()
                .set("sort",
                        NElement.ofObjectBuilder()
                                .set("comparator", NDescribables.describeResolveOrDestruct(c))
                                .set("distinct", removeDuplicates)
                                .build()
                        )
                .build()
                ;
    }

    public Iterator<T> getBase() {
        if (base == null) {
            if (removeDuplicates) {
                base = NIteratorUtils.toTreeSet(it, c).iterator();
            } else {
                List<T> a = NIteratorUtils.toList(it);
                a.sort(c);
                base = a.iterator();
            }
        }
        return base;
    }

    @Override
    public boolean hasNext() {
        return getBase().hasNext();
    }

    @Override
    public T next() {
        return getBase().next();
    }

    @Override
    public String toString() {
        if (removeDuplicates) {
            return "SortDistinct(" + it + ")";
        } else {
            return "SortDuplicates(" + it + ")";
        }
    }
}
