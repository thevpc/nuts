package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;

import java.util.function.Function;

public class NIteratorWithDescription<T> extends NIteratorDelegate<T> {
    private NIterator<T> base;
    private Function<NSession, NElement> description;

    public NIteratorWithDescription(NIterator<T> base, Function<NSession, NElement> description) {
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
    public NElement describe(NSession session) {
        if (description != null) {
            NElement s = description.apply(session);
            if (s != null) {
                return s;
            }
        }
        return super.describe(session);
    }
}
