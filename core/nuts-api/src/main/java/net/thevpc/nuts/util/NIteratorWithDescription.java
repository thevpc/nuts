package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NIteratorWithDescription<T> extends NIteratorDelegate<T> {
    private final NIterator<T> base;
    private final Supplier<NElement> description;
    private final Runnable onClose;

    public NIteratorWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose) {
        this.base = base;
        this.description = description;
        this.onClose = onClose;
    }

    public NIterator<T> getBase() {
        return base;
    }

    @Override
    public NIterator<T> baseIterator() {
        return base;
    }

    public NIterator<T> withDescription(Supplier<NElement> description) {
        return new NIteratorWithDescription<T>(base, description, onClose);
    }

    @Override
    public void close() {
        base.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public String toString() {
        if (description != null) {
            try {
                NElement e = description.get();
                if (e != null) {
                    return e.toString();
                }
            } catch (Exception e) {
                //
            }
        }
        return String.valueOf(base);
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
