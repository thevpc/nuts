package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;

public class NReservedOptionalValidCallable<T> extends NReservedOptionalValid<T> {
    private NCallable<T> value;

    public NReservedOptionalValidCallable(NCallable<T> value) {
        NAssert.requireNonNull(value, "callable");
        this.value = value;
    }

    public T get(NSession session) {
        return value.call(session);
    }
}
