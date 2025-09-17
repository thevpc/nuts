package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

public interface NStableValue<T> {

    static <T> NStableValue<T> of(Supplier<T> supplier) {
        return NConcurrentFactory.of().stableValue(supplier);
    }

    T get();

    boolean isEvaluated();

    boolean isValid();

    boolean isError();

    boolean setIfAbsent(T value);
    boolean computeAndSetIfAbsent(Supplier<T> value);
}
