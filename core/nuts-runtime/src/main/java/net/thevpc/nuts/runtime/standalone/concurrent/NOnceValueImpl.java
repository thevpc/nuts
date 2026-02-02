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
            String id = model.getId();
            NBeanContainer.scopedStack().runWith(NBeanContainer.current(), () -> {
                NOnceValueModel m = store.load(id);
                if (m == null) {
                    m = new NOnceValueModel(id, model.getSupplier());
                    _save(m);
                } else {
                    if (model.getSupplier() != null) {
                        m.setSupplier(model.getSupplier());
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
        Boolean errorState = model.getErrorState();
        if (errorState != null) {
            if (errorState) {
                Throwable e = model.getThrowable();
                sneakyThrow(e);
            }
            return (T) model.getValue();
        }
        synchronized (this) {
            errorState = model.getErrorState();
            if (errorState != null) {
                if (errorState) {
                    Throwable e = model.getThrowable();
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw (Error) e;
                    }
                }
                return (T) model.getValue();
            }
            return doSet((Supplier<T>) model.getSupplier());
        }
    }

    private T doSet(Supplier<T> supplier) {
        try {
            T value = supplier.get();
            model.setValue(value);
            model.setThrowable(null);
            model.setErrorState(false);
            return value;
        } catch (Throwable ex) {
            model.setValue(null);
            model.setThrowable(ex);
            model.setErrorState(false);
            sneakyThrow(ex);
            return null;
        }
    }

    @Override
    public boolean trySupply(Supplier<T> supplier) {
        synchronized (this) {
            Boolean errorState = model.getErrorState();
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
            Boolean errorState = model.getErrorState();
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
        Boolean errorState = model.getErrorState();
        if (errorState != null) {
            return !errorState;
        }
        return false;
    }

    @Override
    public boolean isError() {
        Boolean errorState = model.getErrorState();
        if (errorState != null) {
            return errorState;
        }
        return false;
    }

    public boolean isEvaluated() {
        Boolean errorState = model.getErrorState();
        return (errorState != null);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    @Override
    public NElement describe() {
        Boolean errorState = model.getErrorState();
        NUpletElementBuilder u = NElement.ofUpletBuilder("StableValue")
                .add("evaluated", errorState != null);
        if (errorState != null) {
            u.add("success", !errorState);
            if (errorState) {
                u.add("error", NDescribables.describeResolveOrDestruct(model.getValue()));
            } else {
                u.add("value", NDescribables.describeResolveOrDestruct(model.getThrowable()));
            }
        }
        return u.build();
    }
}
