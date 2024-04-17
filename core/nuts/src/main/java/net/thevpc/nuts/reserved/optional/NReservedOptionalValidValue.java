package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.NSession;

public class NReservedOptionalValidValue<T> extends NReservedOptionalValid<T> {
    private T value;
    public NReservedOptionalValidValue(T value) {
        this.value = value;
    }
    public T get(NSession session) {
        return value;
    }
}
