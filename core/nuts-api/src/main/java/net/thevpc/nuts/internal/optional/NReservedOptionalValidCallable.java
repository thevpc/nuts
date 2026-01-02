package net.thevpc.nuts.internal.optional;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public class NReservedOptionalValidCallable<T> extends NReservedOptionalValid<T> implements Cloneable {

    private final NCallable<NOptional<T>> value;
    private NOptional<T> result;
    private boolean evaluated;

    public NReservedOptionalValidCallable(NCallable<NOptional<T>> value, Supplier<NMsg> msg) {
        super(msg);
        NAssert.requireNonNull(value, "callable");
        this.value = value;
    }

    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NReservedOptionalEmpty<>(message);
    }

    public NOptional<T> withMessage(NMsg message) {
        if (evaluated) {
            if (message == null) {
                return result;
            }
            return result.withMessage(message);
        }
        return new NReservedOptionalValidCallable<>(value, message == null ? (() -> NMsg.ofMissingValue()) : () -> message);
    }

    public NOptional<T> withName(NMsg name) {
        if (evaluated) {
            if (name == null) {
                return result;
            }
            return result.withName(name);
        }
        return new NReservedOptionalValidCallable<>(value, name == null ? (() -> NMsg.ofMissingValue()) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public NOptional<T> withName(String name) {
        if (evaluated) {
            if (name == null) {
                return result;
            }
            return result.withName(name);
        }
        return new NReservedOptionalValidCallable<>(value, name == null ? (() -> NMsg.ofMissingValue()) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNonNull(mapper);
        if (evaluated) {
            if (result != null) {
                if (result.isPresent()) {
                    T v = result.get();
                    try {
                        V r = mapper.apply(v);
                        return NOptional.of(r, getMessage());
                    } catch (Exception ex) {
                        return NOptional.ofError(getMessage(), ex);
                    }
                } else {
                    return (NOptional<V>) result.withMessage(getMessage());
                }
            } else {
                return NOptional.ofEmpty(getMessage());
            }
        }
        return new NReservedOptionalValidCallable<V>(() -> {
            try {
                T y = get();
                if (y != null) {
                    V v = mapper.apply(y);
                    return NOptional.of(v, getMessage());
                } else {
                    return null;
                }
            } catch (Exception ex) {
                return NOptional.ofError(getMessage(), ex);
            }
        }, getMessage());
    }

    @Override
    public T get() {
        if (!evaluated) {
            result = value.call();
            if (result == null) {
                result = NOptional.ofEmpty(getMessage());
            }
            evaluated = true;
        }
        return result.get();
    }


    @Override
    public NElement describe() {
        if (evaluated) {
            return NElement.ofUpletBuilder("Optional")
                    .add("evaluated", true)
                    .add("error", result.isError())
                    .add("empty", result.isEmpty())
                    .add("value", NElementDescribables.describeResolveOrDestruct(result.get()))
                    .build()
                    ;
        } else {
            return NElement.ofUpletBuilder("Optional")
                    .add("evaluated", false)
                    .add("expression", NElementDescribables.describeResolveOrDestruct(value))
                    .build()
                    ;
        }
    }

}
