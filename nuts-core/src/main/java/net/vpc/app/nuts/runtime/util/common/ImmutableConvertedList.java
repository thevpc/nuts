package net.vpc.app.nuts.runtime.util.common;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

import net.vpc.app.nuts.NutsUnsupportedOperationException;

/**
 * Created by vpc on 8/15/14.
 */
public class ImmutableConvertedList<A, B> extends AbstractList<B> {

    private List<A> base;
    private Function<A, B> converter;

    public ImmutableConvertedList(List<A> base, Function<A, B> converter) {
        this.base = base;
        this.converter = converter;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public B get(int index) {
        return converter.apply(base.get(index));
    }

    @Override
    public B set(int index, B element) {
        throw new NutsUnsupportedOperationException(null, "Immutable List");
    }
}
