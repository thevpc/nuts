package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;

import java.util.function.Supplier;

public class NIterableWithDescription<T> implements NIterable<T> {
    private final NIterable<T> base;
    private final Supplier<NElement> nfo;

    public NIterableWithDescription(NIterable<T> base, Supplier<NElement> nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public NIterator<T> iterator() {
        return base.iterator().redescribe(nfo);
    }

    @Override
    public String toString() {
        return "NamedIterable";
    }

    @Override
    public NElement describe() {
        NObjectElement b = NDescribableElementSupplier.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get();
        NElement a = nfo.get();
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .addAll(a.asObject().get())
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
