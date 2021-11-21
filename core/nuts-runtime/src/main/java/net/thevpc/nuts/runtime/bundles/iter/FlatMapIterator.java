package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

public class FlatMapIterator<TT, RR> extends IterInfoNodeAware2Base<RR> {

    private final Iterator<TT> from;
    private final Function<? super TT, ? extends Iterator<? extends RR>> fun;
    Iterator<? extends RR> n;

    public FlatMapIterator(Iterator<TT> from, Function<? super TT, ? extends Iterator<? extends RR>> fun) {
        this.from = from;
        this.fun = fun;
        n = null;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("FlatMap",
                IterInfoNode.resolveOrNull("base", from, session),
                IterInfoNode.resolveOrNull("converter", fun, session)
        );
    }

    @Override
    public boolean hasNext() {
        while (true) {
            if (n == null) {
                if (from.hasNext()) {
                    TT p = from.next();
                    if (p == null) {
                        n = Collections.emptyIterator();
                    } else {
                        n = fun.apply(p);
                        if (n == null) {
                            n = Collections.emptyIterator();
                        }
                    }
                } else {
                    return false;
                }
            }
            if (n.hasNext()) {
                return true;
            } else {
                n = null;
            }
        }
    }

    @Override
    public RR next() {
        return n.next();
    }

    @Override
    public String toString() {
        return "FlattenIterator(" + from + ")";
    }
}
