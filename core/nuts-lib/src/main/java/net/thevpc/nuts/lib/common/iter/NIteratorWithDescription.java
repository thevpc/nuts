package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;

import java.util.function.Supplier;

public class NIteratorWithDescription<T> extends NIteratorDelegate<T> {
    private NIterator<T> base;
    private Supplier<NElement> description;

    public NIteratorWithDescription(NIterator<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public NIterator<T> baseIterator() {
        return base;
    }

    public NIterator<T> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        if (description != null) {
            NElement s = description.get();
            if (s != null) {
                return s;
            }
        }
        return super.describe();
    }
}
