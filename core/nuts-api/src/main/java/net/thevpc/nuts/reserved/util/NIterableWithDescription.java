package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;

public class NIterableWithDescription<T> implements NIterable<T> {
    private final NIterable<T> base;
    private final NEDesc nfo;

    public NIterableWithDescription(NIterable<T> base, NEDesc nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public NIterator<T> iterator() {
        return base.iterator().withDesc(nfo);
    }

    @Override
    public String toString() {
        return "NamedIterable";
    }

    @Override
    public NElement describe() {
        NObjectElement b = NEDesc.describeResolveOr(base, () -> NElements.of().ofObject().build())
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
