package net.thevpc.nuts.util;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 8/15/14.
 */
public class NImmutableConvertedList<A, B> extends AbstractList<B> {

    private final List<A> base;
    private final Function<A, B> converter;

    public NImmutableConvertedList(List<A> base, Function<A, B> converter) {
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
        throw new UnsupportedOperationException("Immutable List");
    }
}
