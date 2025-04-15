package net.thevpc.nuts.runtime.standalone.xtra.time;

import java.util.function.Supplier;

public class NLazySupplier<T> implements Supplier<T> {
    private Supplier<T> supplier;
    private boolean executed;
    private T executedValue;
    private RuntimeException executedError;

    public NLazySupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!executed) {
            try {
                if (supplier == null) {
                    executedValue = null;
                } else {
                    executedValue = supplier.get();
                }
            } catch (RuntimeException e) {
                executedError = e;
            }
            executed = true;
        }
        if (executedError != null) {
            throw executedError;
        }
        return executedValue;
    }

    @Override
    public String toString() {
        if (executed) {
            return String.valueOf(executedError != null ? executedError : executedValue);
        }
        return "NLazySupplier(" + supplier + ")";
    }
}
