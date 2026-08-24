package net.thevpc.nuts.util;

import java.util.function.Function;

/**
 * NCreated class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCreated<T> {
    private final T value;
    private final boolean newValue;

    /**
     * N created.
     *
     * @param value value
     * @param newValue new value
     * @return n created result
     */
    public NCreated(T value, boolean newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    /**
     * Creates a new instance of of new.
     *
     * @param item item
     * @return of new result
     */
    public static <T> NCreated<T> ofNew(T item) {
        return new NCreated<>(item, true);
    }

    /**
     * Creates a new instance of of existing.
     *
     * @param item item
     * @return of existing result
     */
    public static <T> NCreated<T> ofExisting(T item) {
        return new NCreated<>(item, false);
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    public T get() {
        return value;
    }

    /**
     * Checks if is new.
     *
     * @return is new result
     */
    public boolean isNew() {
        return newValue;
    }

    /**
     * Checks if is existing.
     *
     * @return is existing result
     */
    public boolean isExisting() {
        return !newValue;
    }

    /**
     * Map.
     *
     * @param mapper mapper
     * @return map result
     */
    public <V> NCreated<V> map(Function<? super T, ? extends V> mapper) {
        return new NCreated<>(mapper.apply(value), newValue);
    }

    /**
     * Converts to dto.
     *
     * @return to dto result
     */
    public NCreatedDto<T> toDto() {
        return new NCreatedDto<>(value, newValue);
    }
}
