package net.thevpc.nuts;

import net.thevpc.nuts.util.NCallable;

import java.util.function.Supplier;

public class NScopedValue<T> {
    private InheritableThreadLocal<T> holder = new InheritableThreadLocal<>();
    private Supplier<T> defaultSupplier;

    public NScopedValue(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }
    public NScopedValue() {
        this(null);
    }

    public T computeIfAbsent(Supplier<T> supplier) {
        T h = holder.get();
        if (h != null) {
            return h;
        }
        if (defaultSupplier != null) {
            h = defaultSupplier.get();
        }
        if(h == null) {
            h = supplier.get();
            holder.set(h);
        }
        return h;
    }

    public T get() {
        T h = holder.get();
        if (h != null) {
            return h;
        }
        if (defaultSupplier != null) {
            h = defaultSupplier.get();
        }
        return h;
    }

    public void runWith(T value, Runnable r) {
        T old = holder.get();
        // No dependency on equals() + Performance gain
        if (value == old) {
            if (r != null) {
                r.run();
            }
            return;
        }
        holder.set(value);
        try {
            if (r != null) {
                r.run();
            }
        } finally {
            if (old == null) {
                holder.remove();
            } else {
                holder.set(old);
            }
        }
    }

    public <V> V callWith(T value, NCallable<V> r) {
        T old = holder.get();
        // No dependency on equals() + Performance gain
        if (value == old) {
            if (r != null) {
                return r.call();
            }
            return null;
        }
        holder.set(value);
        try {
            if (r != null) {
                return r.call();
            }
        } finally {
            if (old == null) {
                holder.remove();
            } else {
                holder.set(old);
            }
        }
        return null;
    }
}
