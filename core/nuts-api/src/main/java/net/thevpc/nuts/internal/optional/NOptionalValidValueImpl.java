package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

/**
 * NReservedOptionalValidValue class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NOptionalValidValueImpl<T> extends NOptionalValidImpl<T> implements Cloneable{
    private final T value;
    /**
     * N reserved optional valid value.
     *
     * @param value value
     * @param message message
     * @return n reserved optional valid value result
     */
    public NOptionalValidValueImpl(T value, Supplier<NMsg> message) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
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
        return NElement.ofTupleBuilder("Optional")
                .add("evaluated", true)
                .add("empty", false)
                .add("error", false)
                .add("value", NDescribables.describeResolveOrSimplify(value))
                .build()
                ;
    }
}
