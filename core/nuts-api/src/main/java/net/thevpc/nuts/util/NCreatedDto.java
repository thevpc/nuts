package net.thevpc.nuts.util;

import net.thevpc.nuts.NDTO;

import java.util.function.Function;

/**
 * serializable friendly NCreated
 * @param <T>
 */
public class NCreatedDto<T> implements NDTO {
    private final T value;
    private final boolean newValue;

    public NCreatedDto(T value, boolean newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    public T getValue() {
        return value;
    }

    public boolean isNewValue() {
        return newValue;
    }

    public <V> NCreatedDto<V> map(Function<? super T, ? extends V> mapper) {
        return new NCreatedDto<>(mapper.apply(value), newValue);
    }
}
