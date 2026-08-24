package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.pipeline.NIterable;
import net.thevpc.nuts.pipeline.NIterator;

import java.util.function.Supplier;

/**
 * NIterableWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NIterableWithDescription<T> implements NIterable<T> {
    private final NIterable<T> base;
    private final Supplier<NElement> nfo;

    /**
     * N iterable with description.
     *
     * @param base base
     * @param nfo nfo
     * @return n iterable with description result
     */
    public NIterableWithDescription(NIterable<T> base, Supplier<NElement> nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public NIterator<T> iterator() {
        return base.iterator().withDescription(nfo);
    }

    @Override
    public String toString() {
        return "NamedIterable";
    }

    @Override
    public NElement describe() {
        NObjectElement b = NDescribables.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get();
        NElement a = nfo.get();
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .copyFrom(a.asObject().get())
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
