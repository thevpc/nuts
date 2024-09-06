package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.util.NComparator;

import java.util.Comparator;

class NComparatorBase<T> implements NComparator<T> {
    private final Comparator<T> base;
    private final NEDesc nfo;

    public NComparatorBase(Comparator<T> base, NEDesc nfo) {
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
    public NElement describe(NSession session) {
        NObjectElement b = NEDesc.describeResolveOrDestructAsObject(base, session);
        NElement a = nfo.apply(session);
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .addAll(a.asObject().get(session))
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
