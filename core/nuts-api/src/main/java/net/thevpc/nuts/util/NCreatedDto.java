package net.thevpc.nuts.util;

import java.util.function.Function;

/**
 * serializable friendly NCreated
 * @param <T>
 */
public class NCreatedDto<T> implements NDTO {
    private final T value;
    private final boolean newValue;

    /**
     * N created dto.
     *
     * @param value value
     * @param newValue new value
     * @return n created dto result
     */
    public NCreatedDto(T value, boolean newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    /**
     * Returns the value.
     *
     * @return get value result
     */
    public T getValue() {
        return value;
    }

    /**
     * Checks if is new value.
     *
     * @return is new value result
     */
    public boolean isNewValue() {
        return newValue;
    }

    /**
     * Map.
     *
     * @param mapper mapper
     * @return map result
     */
    public <V> NCreatedDto<V> map(Function<? super T, ? extends V> mapper) {
        return new NCreatedDto<>(mapper.apply(value), newValue);
    }
}
