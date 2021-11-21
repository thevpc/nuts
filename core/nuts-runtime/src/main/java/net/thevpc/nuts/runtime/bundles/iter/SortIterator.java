package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class SortIterator<T> extends IterInfoNodeAware2Base<T> {

    private final boolean removeDuplicates;
    private final Iterator<T> it;
    private final Comparator<T> c;
    Iterator<T> base;

    public SortIterator(Iterator<T> it, Comparator<T> c, boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
        this.it = it;
        this.c = c;
        base = null;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("Sort",
                IterInfoNode.resolveOrNull("base", base, session),
                IterInfoNode.resolveOrNull("comparator", c, session),
                IterInfoNode.ofLiteral("distinct", null, removeDuplicates)
        );
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
