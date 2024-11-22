package net.thevpc.nuts.util;

import java.util.function.Function;

public class NCreated<T> {
    private final T value;
    private final boolean newValue;

    public NCreated(T value, boolean newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    public static <T> NCreated<T> ofNew(T item) {
        return new NCreated<>(item, true);
    }

    public static <T> NCreated<T> ofExisting(T item) {
        return new NCreated<>(item, false);
    }

    public T get() {
        return value;
    }

    public boolean isNew() {
        return newValue;
    }

    public boolean isExisting() {
        return !newValue;
    }

    public <V> NCreated<V> map(Function<? super T, ? extends V> mapper) {
        return new NCreated<>(mapper.apply(value), newValue);
    }

    public NCreatedDto<T> toDto() {
        return new NCreatedDto<>(value, newValue);
    }
}
