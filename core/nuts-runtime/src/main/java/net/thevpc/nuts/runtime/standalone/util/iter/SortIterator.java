package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.util.NIterator;

import java.util.Iterator;
import java.util.List;

class SortIterator<T> extends NIteratorBase<T> {

    private final boolean removeDuplicates;
    private final NIterator<T> it;
    private final NComparator<T> c;
    Iterator<T> base;

    public SortIterator(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
        this.it = it;
        this.c = c;
        base = null;
    }

    @Override
    public NElement describe(NSession session) {
        return NDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("sort",
                        NElements.of(session).ofObject()
                                .set("comparator", NDescribables.resolveOrDestruct(c, session))
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
