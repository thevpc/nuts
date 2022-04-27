package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.List;

class SortIterator<T> extends NutsIteratorBase<T> {

    private final boolean removeDuplicates;
    private final NutsIterator<T> it;
    private final NutsComparator<T> c;
    Iterator<T> base;

    public SortIterator(NutsIterator<T> it, NutsComparator<T> c, boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
        this.it = it;
        this.c = c;
        base = null;
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("sort",
                        NutsElements.of(session).ofObject()
                                .set("comparator", NutsDescribables.resolveOrDestruct(c, session))
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
