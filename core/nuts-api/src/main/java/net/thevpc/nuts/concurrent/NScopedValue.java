package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

/**
 * Represents a thread-scoped value that can be inherited by child threads.
 * <p>
 * Each thread has at most one value at a time. Temporary overrides are possible
 * using {@link #runWith(Object, Runnable)} or {@link #callWith(Object, NCallable)}.
 * <p>
 * Note: Only the current value is tracked. Nested or hierarchical contexts are not supported.
 *
 * @param <T> the type of the scoped value
 */
public class NScopedValue<T> {
    /** Holds the thread-local value. */
    private InheritableThreadLocal<T> holder = new InheritableThreadLocal<>();
    /** Supplier to provide a default value if none is set. */
    private Supplier<T> defaultSupplier;

    /**
     * Creates a new {@code NScopedValue} with a default value supplier.
     *
     * @param defaultSupplier the supplier used to provide a default value
     */
    public NScopedValue(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * Creates a new {@code NScopedValue} without a default supplier.
     * <p>
     * Values must then be provided explicitly or via {@link #computeIfAbsent(Supplier)}.
     */
    public NScopedValue() {
        this(null);
    }

    /**
     * Returns the current value for the current thread, computing and setting it
     * if absent.
     * <p>
     * The computation follows this order:
     * <ol>
     *     <li>If a value is already set for this thread, return it.</li>
     *     <li>If a default supplier exists, use it to obtain a value.</li>
     *     <li>If still absent, use the provided supplier to compute and store the value.</li>
     * </ol>
     *
     * @param supplier the supplier used to compute a value if absent
     * @return the current or newly computed value
     */
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

    /**
     * Returns the current value for the current thread.
     * <p>
     * If no value has been set, the default supplier is used if available.
     *
     * @return the current value, or {@code null} if none is set and no default exists
     */
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

    /**
     * Executes a {@link Runnable} with a temporary scoped value.
     * <p>
     * The current thread value is temporarily set to the given {@code value} for
     * the duration of the runnable. After execution, the original value is restored.
     *
     * @param value the value to set temporarily
     * @param r the runnable to execute
     */
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

    /**
     * Executes a {@link NCallable} with a temporary scoped value and returns its result.
     * <p>
     * The current thread value is temporarily set to the given {@code value} for
     * the duration of the callable. After execution, the original value is restored.
     *
     * @param value the value to set temporarily
     * @param r the callable to execute
     * @param <V> the type of the result
     * @return the result of the callable, or {@code null} if the callable is {@code null}
     */
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
