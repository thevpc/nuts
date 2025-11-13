package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;

import java.util.function.Supplier;

/**
 * Represents a stable, lazily-evaluated value that can be computed once and then reused.
 * <p>
 * A {@code NStableValue} may hold a computed value, indicate whether it has been evaluated,
 * and track its validity or error state. It extends {@link Supplier} to provide standard
 * access to the value and {@link NElementDescribable} for descriptive capabilities.
 *
 * @param <T> the type of the value
 */
public interface NStableValue<T> extends Supplier<T>, NElementDescribable {

    /**
     * Creates a stable value from a supplier.
     * <p>
     * The supplied value will be computed lazily and cached internally.
     *
     * @param supplier the supplier that provides the value
     * @param <T> the type of the value
     * @return a new {@code NStableValue} wrapping the given supplier
     */
    static <T> NStableValue<T> of(Supplier<T> supplier) {
        return NConcurrent.of().stableValue(supplier);
    }

    /**
     * Returns the current value of this stable value.
     * <p>
     * If the value has not been computed yet, it will be evaluated at this time.
     *
     * @return the value
     * @throws RuntimeException if computation failed or the value is in an error state
     */
    T get();

    /**
     * Checks whether the value has already been evaluated.
     *
     * @return {@code true} if the value has been evaluated, {@code false} otherwise
     */
    boolean isEvaluated();

    /**
     * Checks whether the current value is valid.
     * <p>
     * A value may become invalid if it is cleared, expired, or encountered an error during computation.
     *
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    boolean isValid();

    /**
     * Checks whether the value is in an error state.
     *
     * @return {@code true} if an error occurred during evaluation, {@code false} otherwise
     */
    boolean isError();

    /**
     * Sets the value if it has not already been set or computed.
     *
     * @param value the value to set
     * @return {@code true} if the value was successfully set, {@code false} if a value was already present
     */
    boolean setIfAbsent(T value);

    /**
     * Computes the value using the given supplier and sets it if it has not already been set.
     *
     * @param value the supplier providing the value
     * @return {@code true} if the value was successfully computed and set, {@code false} if a value was already present
     */
    boolean computeAndSetIfAbsent(Supplier<T> value);
}
