package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.util.NPredicates;

import java.util.HashSet;
import java.util.function.Function;

class DistinctWithConverterPredicate<F, T> extends NPredicates.BasePredicate<F> implements NDescribable {
    private final Function<F, T> converter;
    HashSet<T> visited;

    public DistinctWithConverterPredicate(Function<F, T> converter) {
        this.converter = converter;
        visited = new HashSet<>();
    }

    @Override
    public boolean test(F value) {
        T t = converter.apply(value);
        if (visited.contains(t)) {
            return false;
        }
        visited.add(t);
        return true;
    }

    @Override
    public String toString() {
        return "DistinctConverter[" + converter + "]";
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofObject()
                .set("distinctBy", NDescribables.resolveOrDestruct(converter, session))
                .build()
                ;
    }

}
