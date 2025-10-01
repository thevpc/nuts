package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;

import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public interface NStableValue<T> extends Supplier<T>, NElementDescribable {

    static <T> NStableValue<T> of(Supplier<T> supplier) {
        return NConcurrent.of().stableValue(supplier);
    }

    T get();

    boolean isEvaluated();

    boolean isValid();

    boolean isError();

    boolean setIfAbsent(T value);

    boolean computeAndSetIfAbsent(Supplier<T> value);
}
