package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.text.NMsg;
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

    @Override
    public NElement describe() {
        return NElement.ofUpletBuilder("Optional")
                .add("evaluated", true)
                .add("empty", false)
                .add("error", false)
                .add("value", NElementDescribables.describeResolveOrDestruct(value))
                .build()
                ;
    }
}
