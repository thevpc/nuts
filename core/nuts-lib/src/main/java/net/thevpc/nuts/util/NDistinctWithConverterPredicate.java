package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.spi.base.AbstractNPredicate;

import java.util.HashSet;
import java.util.function.Function;

class NDistinctWithConverterPredicate<F, T> extends AbstractNPredicate<F> {
    private final Function<F, T> converter;
    HashSet<T> visited;

    public NDistinctWithConverterPredicate(Function<F, T> converter) {
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
    public NElement describe() {
        return NElements.ofObjectBuilder()
                .set("distinctBy", NEDesc.describeResolveOrDestruct(converter))
                .build()
                ;
    }

}
