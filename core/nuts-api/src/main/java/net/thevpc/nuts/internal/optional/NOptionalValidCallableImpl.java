package net.thevpc.nuts.internal.optional;

import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * NReservedOptionalValidCallable class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NOptionalValidCallableImpl<T> extends NOptionalValidImpl<T> implements Cloneable {

    private final NCallable<NOptional<T>> value;
    private NOptional<T> result;
    private boolean evaluated;

    /**
     * N reserved optional valid callable.
     *
     * @param value value
     * @param msg msg
     * @return n reserved optional valid callable result
     */
    public NOptionalValidCallableImpl(NCallable<NOptional<T>> value, Supplier<NMsg> msg) {
      /**
       * Super.
       *
       * @param msg msg
       */
        super(msg);
        NAssert.requireNamedNonNull(value, "callable");
        this.value = value;
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NOptionalEmptyImpl<>(message);
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(NMsg message) {
        if (evaluated) {
            if (message == null) {
                return result;
            }
            return result.withMessage(message);
        }
        return new NOptionalValidCallableImpl<>(value, message == null ? (() -> NMsg.ofMissingValue()) : () -> message);
    }

    /**
     * With name.
     *
     * @param name name
     * @return with name result
     */
    public NOptional<T> withName(NMsg name) {
        if (evaluated) {
            if (name == null) {
                return result;
            }
            return result.withName(name);
        }
        return new NOptionalValidCallableImpl<>(value, name == null ? (() -> NMsg.ofMissingValue()) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public NOptional<T> withName(String name) {
        if (evaluated) {
            if (name == null) {
                return result;
            }
            return result.withName(name);
        }
        return new NOptionalValidCallableImpl<>(value, name == null ? (() -> NMsg.ofMissingValue()) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNamedNonNull(mapper);
        if (evaluated) {
            if (result != null) {
                if (result.isPresent()) {
                    T v = result.get();
                    try {
                        V r = mapper.apply(v);
                        return NOptional.of(r, message());
                    } catch (Exception ex) {
                        return NOptional.ofError(message(), ex);
                    }
                } else {
                  /**
                   * Return.
                   *
                   * @param result.withMessage(message() result.with message(message()
                   */
                    return (NOptional<V>) result.withMessage(message());
                }
            } else {
                return NOptional.ofEmpty(message());
            }
        }
        return new NOptionalValidCallableImpl<V>(() -> {
            try {
                T y = get();
                if (y != null) {
                    V v = mapper.apply(y);
                    return NOptional.of(v, message());
                } else {
                    return null;
                }
            } catch (Exception ex) {
                return NOptional.ofError(message(), ex);
            }
        }, message());
    }

    @Override
    public T get() {
        if (!evaluated) {
            result = value.call();
            if (result == null) {
                result = NOptional.ofEmpty(message());
            }
            evaluated = true;
        }
        return result.get();
    }


    @Override
    public NElement describe() {
        if (evaluated) {
            return NElement.ofTupleBuilder("Optional")
                    .add("evaluated", true)
                    .add("error", result.isError())
                    .add("empty", result.isEmpty())
                    .add("value", NDescribables.describeResolveOrSimplify(result.get()))
                    .build()
                    ;
        } else {
            return NElement.ofTupleBuilder("Optional")
                    .add("evaluated", false)
                    .add("expression", NDescribables.describeResolveOrSimplify(value))
                    .build()
                    ;
        }
    }

}
