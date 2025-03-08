package net.thevpc.nuts.reserved.optional;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class NReservedOptionalValidCallable<T> extends NReservedOptionalValid<T> implements Cloneable {

    private final NCallable<NOptional<T>> value;
    private NOptional<T> result;
    private boolean evaluated;
    private Supplier<NMsg> msg;

    public NReservedOptionalValidCallable(NCallable<NOptional<T>> value) {
        NAssert.requireNonNull(value, "callable");
        this.value = value;
    }

    public NReservedOptionalValidCallable(NCallable<NOptional<T>> value, Supplier<NMsg> msg) {
        NAssert.requireNonNull(value, "callable");
        this.value = value;
        this.msg = msg;
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
        Objects.requireNonNull(mapper);
        if (evaluated) {
            if (result != null) {
                if (result.isPresent()) {
                    T v = result.get();
                    try {
                        V r = mapper.apply(v);
                        if (msg != null) {
                            return NOptional.of(r, msg);
                        }
                        return NOptional.of(r);
                    } catch (Exception ex) {
                        if (msg != null) {
                            return NOptional.ofError(msg, ex);
                        }
                        return NOptional.ofError(ex);
                    }
                } else {
                    if (msg != null) {
                        return (NOptional<V>) result.withMessage(msg);
                    }
                    return (NOptional<V>) result;
                }
            } else {
                return NOptional.ofEmpty(getMessage());
            }
        }
        return new NReservedOptionalValidCallable<V>(() -> {
            if (msg == null) {
                try {
                    T y = get();
                    if (y != null) {
                        V v = mapper.apply(y);
                        return NOptional.of(v);
                    } else {
                        return null;
                    }
                } catch (Exception ex) {
                    if (msg != null) {
                        return NOptional.ofError(msg, ex);
                    }
                    return NOptional.ofError(ex);
                }
            } else {
                try {
                    T y = get();
                    if (y != null) {
                        V v = mapper.apply(y);
                        return NOptional.of(v, msg);
                    } else {
                        return null;
                    }
                } catch (Exception ex) {
                    if (msg != null) {
                        return NOptional.ofError(msg, ex);
                    }
                    return NOptional.ofError(ex);
                }
            }
        }, msg);
    }

    @Override
    public Supplier<NMsg> getMessage() {
        return msg == null ? () -> NMsg.ofMissingValue() : msg;
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
}
