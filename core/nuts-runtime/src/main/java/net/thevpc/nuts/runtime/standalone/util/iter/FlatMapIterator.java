package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NSession;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

public class FlatMapIterator<TT, RR> extends NIteratorBase<RR> {

    private final Iterator<TT> from;
    private final Function<? super TT, ? extends Iterator<? extends RR>> converter;
    Iterator<? extends RR> current;

    public FlatMapIterator(Iterator<TT> from, Function<? super TT, ? extends Iterator<? extends RR>> converter) {
        this.from = from;
        this.converter = converter;
        current = null;
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session)
                .ofObject()
                .set("type","FlatMap")
                .set("base", NDescribables.resolveOrDestruct(from, session))
                .set("mapper", NDescribables.resolveOrDestruct(converter, session))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        while (true) {
            if (current == null) {
                if (from.hasNext()) {
                    TT p = from.next();
                    if (p == null) {
                        current = Collections.emptyIterator();
                    } else {
                        current = converter.apply(p);
                        if (current == null) {
                            current = Collections.emptyIterator();
                        }
                    }
                } else {
                    return false;
                }
            }
            if (current.hasNext()) {
                return true;
            } else {
                current = null;
            }
        }
    }

    @Override
    public RR next() {
        return current.next();
    }

    @Override
    public String toString() {
        return "FlattenIterator(" + from + ")";
    }
}
