package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.util.NComparator;

import java.util.Comparator;
import java.util.function.Supplier;

class NComparatorBase<T> implements NComparator<T> {
    private final Comparator<T> base;
    private final Supplier<NElement> nfo;

    public NComparatorBase(Comparator<T> base, Supplier<NElement> nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public int compare(T o1, T o2) {
        return base.compare(o1, o2);
    }

    @Override
    public String toString() {
        return "NamedComparator{" + base + '}';
    }

    @Override
    public NElement describe() {
        NObjectElement b = NDescribables.describeResolveOrDestructAsObject(base);
        NElement a = nfo.get();
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .addAll(a.asObject().get())
                    .build()
                    ;
        } else {
            return b.builder()
                    .set("name", a)
                    .build()
                    ;
        }
    }
}
