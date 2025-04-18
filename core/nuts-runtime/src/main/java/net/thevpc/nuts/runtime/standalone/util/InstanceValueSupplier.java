package net.thevpc.nuts.runtime.standalone.util;

import java.util.function.Function;

public class InstanceValueSupplier<X,T> implements Function<X,T> {
    private final T any;

    public InstanceValueSupplier(T any) {
        this.any = any;
    }


    @Override
    public T apply(X x) {
        return any;
    }

    @Override
    public String toString() {
        return String.valueOf(any);
    }
}
