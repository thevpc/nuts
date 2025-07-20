package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.util.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class NReservedOptionalValid<T> extends NReservedOptionalImpl<T> implements Cloneable {


    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        T y = get();
        if (y != null) {
            try {
                return NOptional.of(mapper.apply(y));
            } catch (Exception ex) {
                return NOptional.ofError(getMessage(), ex);
            }
        } else {
            return NOptional.ofEmpty(getMessage());
        }
    }

    @Override
    public Optional<T> asOptional() {
        return Optional.of(get());
    }

    @Override
    public T get(Supplier<NMsg> message) {
        return get();
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public NOptional<T> ifBlankEmpty() {
        if (isBlank()) {
            return NOptional.ofEmpty(NMsg::ofMissingValue);
        }
        return this;
    }

    @Override
    public Supplier<NMsg> getMessage() {
        return NMsg::ofMissingValue;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(get());
    }

    @Override
    public String toString() {
        return "Optional@" + System.identityHashCode(this) + "=" + get();
    }

    @Override
    public NOptional<T> withDefault(T value) {
        return this;
    }

    @Override
    public NOptional<T> withoutDefault() {
        return this;
    }

    @Override
    public T orDefault() {
        return get();
    }

    @Override
    public NOptional<T> orDefaultOptional() {
        return this;
    }

    @Override
    public NOptional<T> withDefault(Supplier<T> value) {
        return this;
    }

    @Override
    public NOptional<T> withDefaultOptional(Supplier<NOptional<T>> value) {
        return this;
    }

    @Override
    public boolean isNull() {
        return get() == null;
    }

    @Override
    public NOptionalType getType() {
        return NOptionalType.PRESENT;
    }

    @Override
    public NOptional<T> withExceptionFactory(ExceptionFactory exceptionFactory) {
        return this;
    }
}
