package net.thevpc.nuts.runtime.standalone.util;

import java.util.function.Supplier;

public class StaticSupplier<V> implements Supplier<V> {
    private final V x;

    public static <V> Supplier<V> of(V x) {
        if(x==null){
            return null;
        }
        return new StaticSupplier<V>(x);
    }

    public StaticSupplier(V x) {
        this.x = x;
    }

    @Override
    public V get() {
        return x;
    }
}
