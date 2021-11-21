package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsPredicates;
import net.thevpc.nuts.NutsSession;

import java.util.HashSet;
import java.util.function.Function;

class DistinctWithConverterPredicate<F, T> extends NutsPredicates.BasePredicate<F> implements IterInfoNodeAware {
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
    public IterInfoNode info(NutsSession session) {
        return IterInfoNode.ofLiteralType( "DistinctBy", "distinctBy",  null,
                IterInfoNode.resolveOrNull("converter", converter, session)
        );
    }

}
