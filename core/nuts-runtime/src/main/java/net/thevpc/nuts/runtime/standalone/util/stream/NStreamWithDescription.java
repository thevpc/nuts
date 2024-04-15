package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

public class NStreamWithDescription<T> extends NStreamDelegate<T> {
    private NStreamBase<T> base;
    private NEDesc description;

    public NStreamWithDescription(NStreamBase<T> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public NStream<T> baseStream() {
        return base;
    }

    @Override
    public NStream<T> withDesc(NEDesc description) {
        this.description=description;
        return this;
    }

    @Override
    public NIterator<T> iterator() {
        return super.iterator().withDesc(description);
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
