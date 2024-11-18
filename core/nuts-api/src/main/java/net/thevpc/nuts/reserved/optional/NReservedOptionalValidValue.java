package net.thevpc.nuts.reserved.optional;

public class NReservedOptionalValidValue<T> extends NReservedOptionalValid<T> implements Cloneable{
    private final T value;
    public NReservedOptionalValidValue(T value) {
        this.value = value;
    }
    @Override
    public T get() {
        return value;
    }
}
