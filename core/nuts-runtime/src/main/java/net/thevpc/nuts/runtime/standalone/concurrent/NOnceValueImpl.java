package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NUpletElementBuilder;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NAssert;

import java.util.function.Supplier;

public final class NOnceValueImpl<T> implements NOnceValue<T> {

    private final NOnceValueStore store;
    private NOnceValueModel model;

    NOnceValueImpl(String id, Supplier<T> supplier, NOnceValueStore store) {
        NAssert.requireNamedNonNull(supplier, "supplier");
        this.store = store;
        this.model = new NOnceValueModel(NAssert.requireNamedNonNull(id, "id"), supplier);
        reload();
    }


    public void reload() {
        synchronized (this) {
            String id = model.id();
            NBeanContainer.scopedStack().runWith(NBeanContainer.current(), () -> {
                NOnceValueModel m = store.load(id);
                if (m == null) {
                    m = new NOnceValueModel(id, model.supplier());
                    _save(m);
                } else {
                    if (model.supplier() != null) {
                        m.supplier(model.supplier());
                        store.save(m);
                    }
                }
                model = m;
            });
        }
    }

    private void _save(NOnceValueModel model) {
        this.model = model;
        NBeanContainer.scopedStack().runWith(NBeanContainer.current(), () -> {
            store.save(model);
        });
    }

    // Factory methods
    public T get() {
        Boolean errorState = model.errorState();
        if (errorState != null) {
            if (errorState) {
                Throwable e = model.error();
                sneakyThrow(e);
            }
            return (T) model.value();
        }
        synchronized (this) {
            errorState = model.errorState();
            if (errorState != null) {
                if (errorState) {
                    Throwable e = model.error();
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw (Error) e;
                    }
                }
                return (T) model.value();
            }
            return doSet((Supplier<T>) model.supplier());
        }
    }

    private T doSet(Supplier<T> supplier) {
        try {
            T value = supplier.get();
            model.value(value);
            model.error(null);
            model.errorState(false);
            return value;
        } catch (Throwable ex) {
            model.value(null);
            model.error(ex);
            model.errorState(false);
            sneakyThrow(ex);
            return null;
        }
    }

    @Override
    public boolean trySupply(Supplier<T> supplier) {
        synchronized (this) {
            Boolean errorState = model.errorState();
            if (errorState != null) {
                doSet(supplier);
                return true;
            }
        }
        return false;
    }

    @Override
    public T orElseSet(Supplier<T> value) {
        trySupply(value);
        return get();
    }


    @Override
    public T orElse(T value) {
        synchronized (this) {
            Boolean errorState = model.errorState();
            if (errorState != null && !errorState) {
                return value;
            }
            return get();
        }
    }

    public boolean trySet(T value) {
        return trySupply(() -> value);
    }

    @Override
    public boolean isValid() {
        Boolean errorState = model.errorState();
        if (errorState != null) {
            return !errorState;
        }
        return false;
    }

    @Override
    public boolean isError() {
        Boolean errorState = model.errorState();
        if (errorState != null) {
            return errorState;
        }
        return false;
    }

    public boolean isEvaluated() {
        Boolean errorState = model.errorState();
        return (errorState != null);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    @Override
    public NElement describe() {
        Boolean errorState = model.errorState();
        NUpletElementBuilder u = NElement.ofUpletBuilder("OnceValue")
                .add("evaluated", errorState != null);
        if (errorState != null) {
            u.add("success", !errorState);
            if (errorState) {
                u.add("error", NDescribables.describeResolveOrSimplify(model.value()));
            } else {
                u.add("value", NDescribables.describeResolveOrSimplify(model.error()));
            }
        }
        return u.build();
    }
}
