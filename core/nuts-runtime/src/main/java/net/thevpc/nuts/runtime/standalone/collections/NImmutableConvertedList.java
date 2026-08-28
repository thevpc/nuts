package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.util.NImmutable;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 8/15/14.
 */
@NImmutable
public class NImmutableConvertedList<A, B> extends AbstractList<B>{

    private final List<A> base;
    private final Function<A, B> converter;

    /**
     * N immutable converted list.
     *
     * @param base base
     * @param converter converter
     * @return n immutable converted list result
     */
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
        /**
         * Unsupported operation exception.
         *
         * @param List" list"
         * @return unsupported operation exception result
         */
        throw new UnsupportedOperationException("Immutable List");
    }
}
