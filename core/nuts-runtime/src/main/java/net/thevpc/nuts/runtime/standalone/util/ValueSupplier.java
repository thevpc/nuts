package net.thevpc.nuts.runtime.standalone.util;

import java.util.function.Supplier;

public class ValueSupplier<T> implements Supplier<T> {
    private final T any;

    public ValueSupplier(T any) {
        this.any = any;
    }

    @Override
    public T get() {
        return any;
    }

    @Override
    public String toString() {
        return String.valueOf(any);
    }
}
