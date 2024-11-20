package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NIterator;

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
        return NEDesc.describeResolveOrDestructAsObject(base)
                .builder()
                .set("sort",
                        NElements.of().ofObject()
                                .set("comparator", NEDesc.describeResolveOrDestruct(c))
                                .set("distinct", removeDuplicates)
                                .build()
                        )
                .build()
                ;
    }

    public Iterator<T> getBase() {
        if (base == null) {
            if (removeDuplicates) {
                base = IteratorUtils.toTreeSet(it, c).iterator();
            } else {
                List<T> a = IteratorUtils.toList(it);
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
