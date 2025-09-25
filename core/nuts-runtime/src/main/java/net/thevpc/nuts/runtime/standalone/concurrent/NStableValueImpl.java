package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.concurrent.NStableValue;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NUpletElementBuilder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class NStableValueImpl<T> implements NStableValue<T> {

    private final AtomicReference<Evaluated> state;
    private final Supplier<T> supplier;

    private static class Evaluated {
        boolean err;
        Object value;

        public Evaluated(boolean err, Object value) {
            this.err = err;
            this.value = value;
        }
    }

    private NStableValueImpl(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        state = new AtomicReference<>(null);
        this.supplier = supplier;
    }


    // Factory methods
    public static <T> NStableValueImpl<T> of(Supplier<T> supplier) {
        return new NStableValueImpl<>(supplier);
    }

    public T get() {
        Evaluated current = state.get();
        if (current != null) {
            if (current.err) {
                Throwable e = (Throwable) current.value;
                sneakyThrow(e);
            }
            return (T) current.value;
        }
        synchronized (this) {
            current = state.get();
            if (current != null) {
                if (current.err) {
                    Throwable e = (Throwable) current.value;
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw (Error) e;
                    }
                }
                return (T) current.value;
            }
            try {
                T value = supplier.get();
                state.set(new Evaluated(false, value));
                return value;
            } catch (Throwable ex) {
                state.set(new Evaluated(true, ex));  // write-once
                sneakyThrow(ex);
                return null;
            }
        }
    }

    @Override
    public boolean computeAndSetIfAbsent(Supplier<T> supplier) {
        synchronized (this) {
            if (state.get() == null) {
                try {
                    T value = supplier.get();
                    state.set(new Evaluated(false, value));
                } catch (Throwable ex) {
                    state.set(new Evaluated(true, ex));  // write-once
                    sneakyThrow(ex);
                }
                return true;
            }
        }
        return false;
    }

    public boolean setIfAbsent(T value) {
        synchronized (this) {
            if (state.get() == null) {
                state.set(new Evaluated(false, value));
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean isValid() {
        Evaluated v = state.get();
        return v != null && !v.err;
    }

    @Override
    public boolean isError() {
        Evaluated v = state.get();
        return v != null && v.err;
    }

    public boolean isEvaluated() {
        return state.get() != null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    @Override
    public NElement describe() {
        Evaluated v = state.get();
        NUpletElementBuilder u = NElement.ofUpletBuilder("StableValue")
                .add("evaluated", v != null);
        if (v != null) {
            u.add("success", !v.err);
            if (v.err) {
                u.add("error", NElementDescribables.describeResolveOrDestruct(v.value));
            } else {
                u.add("value", NElementDescribables.describeResolveOrDestruct(v.value));
            }
        }
        return u.build();
    }
}
