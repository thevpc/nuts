package net.thevpc.nuts.runtime.standalone.io.cache;


public interface CachedSupplier<T> {
    enum NCacheLevel {
        NONE,
        MEM,
        STORE
    }

    T getValue();
    T getValue(NCacheLevel level);
}
