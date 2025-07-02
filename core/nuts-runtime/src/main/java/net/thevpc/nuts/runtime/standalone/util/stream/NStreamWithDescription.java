package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.function.Supplier;

public class NStreamWithDescription<T> extends NStreamDelegate<T> {
    private NStreamBase<T> base;
    private Supplier<NElement> description;

    public NStreamWithDescription(NStreamBase<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public NStream<T> baseStream() {
        return base;
    }

    @Override
    public NStream<T> redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public NIterator<T> iterator() {
        return super.iterator().redescribe(description);
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

    @Override
    public void close() {
        base.close();
    }
}
