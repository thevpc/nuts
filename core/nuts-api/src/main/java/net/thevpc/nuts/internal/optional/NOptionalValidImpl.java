package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * NReservedOptionalValid class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NOptionalValidImpl<T> extends NOptionalImpl<T> implements Cloneable {

    /**
     * N reserved optional valid.
     *
     * @param message message
     * @return n reserved optional valid result
     */
    public NOptionalValidImpl(Supplier<NMsg> message) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        T y = get();
        if (y != null) {
            try {
                return NOptional.of(mapper.apply(y));
            } catch (Exception ex) {
                return NOptional.ofError(message(), ex);
            }
        } else {
            return NOptional.ofEmpty(message());
        }
    }

    @Override
    public Optional<T> asOptional() {
        return Optional.of(get());
    }

    @Override
    public T get(Supplier<NMsg> message) {
        /**
         * Returns the get.
         *
         * @return get result
         */
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

    /**
     * On blank empty.
     *
     * @return on blank empty result
     */
    public NOptional<T> onBlankEmpty() {
        if (isBlank()) {
            return NOptional.ofEmpty(message());
        }
        return this;
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
        /**
         * Returns the get.
         *
         * @return get result
         */
        return get();
    }

    @Override
    public T orDefault(Class<T> defaultType) {
        /**
         * Returns the get.
         *
         * @return get result
         */
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
    public NOptionalType type() {
        return NOptionalType.PRESENT;
    }

}
