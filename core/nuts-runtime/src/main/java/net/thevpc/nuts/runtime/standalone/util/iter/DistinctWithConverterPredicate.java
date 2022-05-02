package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.util.NutsDescribable;
import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.util.NutsPredicates;

import java.util.HashSet;
import java.util.function.Function;

class DistinctWithConverterPredicate<F, T> extends NutsPredicates.BasePredicate<F> implements NutsDescribable {
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
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofObject()
                .set("distinctBy", NutsDescribables.resolveOrDestruct(converter, session))
                .build()
                ;
    }

}
