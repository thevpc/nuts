package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

public class NReservedOptionalValidValue<T> extends NReservedOptionalValid<T> implements Cloneable{
    private final T value;
    public NReservedOptionalValidValue(T value) {
        this.value = value;
    }

    @Override
    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return this;
    }

    @Override
    public NOptional<T> withMessage(NMsg message) {
        return this;
    }

    @Override
    public NOptional<T> withName(NMsg name) {
        return this;
    }

    @Override
    public NOptional<T> withName(String name) {
        return this;
    }

    @Override
    public T get() {
        return value;
    }
}
