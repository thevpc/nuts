package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.NSession;

public class NReservedOptionalValidValue<T> extends NReservedOptionalValid<T> implements Cloneable{
    private final T value;
    public NReservedOptionalValidValue(T value) {
        this.value = value;
    }
    @Override
    public T get(NSession session) {
        return value;
    }
}
