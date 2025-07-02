package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NIteratorWithDescription<T> extends NIteratorDelegate<T> {
    private final NIterator<T> base;
    private final Supplier<NElement> description;

    public NIteratorWithDescription(NIterator<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    public NIterator<T> getBase() {
        return base;
    }

    @Override
    public NIterator<T> baseIterator() {
        return base;
    }

    public NIterator<T> redescribe(Supplier<NElement> description) {
        return new NIteratorWithDescription<T>(base, description);
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
