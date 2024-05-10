package net.thevpc.nuts.util;

public class NCreated<T> {
    private final T value;
    private final boolean newValue;

    public NCreated(T value, boolean newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    public static <T> NCreated<T> ofNew(T item) {
        return new NCreated<>(item,true);
    }

    public static <T> NCreated<T> ofExisting(T item) {
        return new NCreated<>(item,false);
    }

    public T getValue() {
        return value;
    }

    public boolean isNewValue() {
        return newValue;
    }
}
