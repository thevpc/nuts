package net.thevpc.nuts.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * A thread-scoped stack of values that can be inherited by child threads.
 * <p>
 * Each thread maintains its own stack of values. The "current value" for the thread
 * is the value at the top of the stack. Previous values in the stack are preserved
 * and can be accessed if needed using {@link #peek(int)} or {@link #getStackSnapshot()}.
 * <p>
 * This class is useful for hierarchical or nested contexts where:
 * <ul>
 *     <li>Each thread may temporarily override a context value for a block of code.</li>
 *     <li>Nested scopes must maintain their own context without losing the outer context.</li>
 *     <li>The context may depend not only on the current value but also on parent or stacked values.</li>
 * </ul>
 * <p>
 * Values are stored using an {@link InheritableThreadLocal}, so child threads inherit
 * the parent thread's stack.
 *
 * @param <T> the type of values stored in the stack
 */
public class NScopedStack<T> {
    /** The thread-local stack holding values for each thread. */
    private InheritableThreadLocal<Stack<T>> holder = new InheritableThreadLocal<>();
    /** Optional supplier to provide a default value when the stack is empty. */
    private Supplier<T> defaultSupplier;

    /**
     * Constructs a new {@code NScopedStack} with a default supplier.
     *
     * @param defaultSupplier a supplier providing a default value when the stack is empty
     */
    public NScopedStack(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * Returns the current value at the top of the stack for the current thread.
     * <p>
     * If the stack is empty, the {@link #defaultSupplier} is used if available.
     *
     * @return the current top value or the default value if the stack is empty, {@code null} if none
     */
    public T get() {
        Stack<T> s = holder.get();
        T h = null;
        if (s != null) {
            h = s.peek();
            if (h != null) {
                return h;
            }
        }
        if (defaultSupplier != null) {
            h = defaultSupplier.get();
        }
        return h;
    }

    /**
     * Sets or replaces the default supplier for this stack.
     *
     * @param defaultSupplier the new default supplier
     * @return this {@code NScopedStack} instance for method chaining
     */
    public NScopedStack<T> setDefaultSupplier(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
        return this;
    }

    /**
     * Temporarily pushes a value onto the stack for the current thread, executes
     * the provided {@link Runnable}, and then restores the previous stack state.
     * <p>
     * This ensures that nested or temporary overrides do not affect outer scopes.
     *
     * @param value the value to push temporarily onto the stack
     * @param r the runnable to execute; may be {@code null}
     */
    public void runWith(T value, Runnable r) {
        if (value == null) {
            if (r != null) {
                r.run();
            }
            return;
        }
        Stack<T> s = holder.get();

        if (s != null && !s.isEmpty()) {
            T old = s.peek();
            if (old == value) {
                if (r != null) {
                    r.run();
                }
            } else {
                s.push(value);
                try {
                    if (r != null) {
                        r.run();
                    }
                } finally {
                    s.pop();
                    if (s.isEmpty()) {
                        holder.remove();
                    }
                }
            }
        } else {
            if (s == null) {
                holder.set(s = new Stack<>());
            }
            s.push(value);
            try {
                if (r != null) {
                    r.run();
                }
            } finally {
                s.pop();
                if (s.isEmpty()) {
                    holder.remove();
                }
            }
        }
    }

    /**
     * Temporarily pushes a value onto the stack for the current thread, executes
     * the provided {@link NCallable}, and returns its result.
     * <p>
     * After execution, the previous stack state is restored.
     *
     * @param value the value to push temporarily onto the stack
     * @param c the callable to execute; may be {@code null}
     * @param <V> the type of the result
     * @return the result of the callable, or {@code null} if the callable is {@code null}
     */
    public <V> V callWith(T value, NCallable<V> c) {
        if (value == null) {
            if (c != null) {
                return c.call();
            }
            return null;
        }
        Stack<T> s = holder.get();

        if (s != null && !s.isEmpty()) {
            T old = s.peek();
            if (old == value) {
                if (c != null) {
                    return c.call();
                }
                return null;
            } else {
                s.push(value);
                try {
                    if (c != null) {
                        return c.call();
                    }
                    return null;
                } finally {
                    s.pop();
                    if (s.isEmpty()) {
                        holder.remove();
                    }
                }
            }
        } else {
            if (s == null) {
                holder.set(s = new Stack<>());
            }
            s.push(value);
            try {
                if (c != null) {
                    return c.call();
                }
                return null;
            } finally {
                s.pop();
                if (s.isEmpty()) {
                    holder.remove();
                }
            }
        }
    }

    /**
     * Returns whether the stack for the current thread is empty.
     *
     * @return {@code true} if the stack is empty or has not been initialized; {@code false} otherwise
     */
    public boolean isEmpty() {
        Stack<T> s = holder.get();
        return s == null || s.isEmpty();
    }


    /**
     * Returns the depth (number of values) of the stack for the current thread.
     *
     * @return the number of values in the stack, or {@code 0} if empty or uninitialized
     */
    public int depth() {
        Stack<T> s = holder.get();
        return s == null ? 0 : s.size();
    }

    /**
     * Returns a snapshot of the current stack values for the current thread.
     * <p>
     * Modifying the returned list does not affect the actual stack.
     *
     * @return a list containing the current stack values, empty if the stack is empty or uninitialized
     */
    public List<T> getStackSnapshot() {
        Stack<T> s = holder.get();
        return s == null ? Collections.emptyList() : new ArrayList<>(s);
    }

    /**
     * Returns the value at the specified level from the top of the stack.
     * <p>
     * Level 0 corresponds to the top of the stack, level 1 to the next element down, and so on.
     *
     * @param levelFromTop the level from the top (0 = top)
     * @return the value at the specified level, or {@code null} if the level is invalid or stack is empty
     */
    public T peek(int levelFromTop) {
        Stack<T> s = holder.get();
        if (s == null || s.isEmpty()) return null;
        int index = s.size() - 1 - levelFromTop;
        return (index >= 0) ? s.get(index) : null;
    }
}
