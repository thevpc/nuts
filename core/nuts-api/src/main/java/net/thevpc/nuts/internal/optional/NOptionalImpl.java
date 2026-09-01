package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * NReservedOptionalImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NOptionalImpl<T> implements NOptional<T>, Cloneable {
    private NOptionalExceptionFactory exceptionFactory;
    private final Supplier<NMsg> message;

    /**
     * N reserved optional impl.
     *
     * @param message message
     * @return n reserved optional impl result
     */
    public NOptionalImpl(Supplier<NMsg> message) {
        if (message == null) {
            message = NMsg::ofMissingValue;
        }
        this.message = message;
    }

    @Override
    public Supplier<NMsg> message() {
        return message;
    }


    @Override
    public <V> NOptional<V> instanceOf(Class<V> targetClass) {
        if (targetClass == null) {
            return (NOptional<V>) this;
        }
        return map(a -> {
            if (targetClass.isInstance(a)) {
                return (V) a;
            }
            return null;
        });
    }

    @Override
    public NStream<T> stream() {
        return NStream.ofOptional(this);
    }

    @Override
    public Stream<T> jstream() {
        if (!isPresent()) {
            return Stream.empty();
        }
        return Stream.of(get());
    }

    @Override
    public boolean orTrue() {
      /**
       * Return.
       *
       * @param this).orElse(true this).or else(true
       */
        return ((NOptional<Boolean>) this).orElse(true);
    }

    @Override
    public boolean orFalse() {
      /**
       * Return.
       *
       * @param this).orElse(false this).or else(false
       */
        return ((NOptional<Boolean>) this).orElse(false);
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    public T get() {
        /**
         * Returns the get.
         *
         * @param null null
         * @return get result
         */
        return get(null);
    }

    /**
     * Flat map.
     *
     * @param mapper mapper
     * @return flat map result
     */
    public <V> NOptional<V> flatMap(Function<T, NOptional<V>> mapper) {
        NAssert.requireNamedNonNull(mapper);
        if (isPresent()) {
            NOptional<V> b = mapper.apply(get());
            if (b == null) {
                return NOptional.ofEmpty(message());
            }
            return b;
        }
        return (NOptional<V>) this;
    }

    @Override
    public <V> NOptional<V> mapIfPresent(Function<T, V> mapper) {
        if (isPresent()) {
            /**
             * Map.
             *
             * @param mapper mapper
             * @return map result
             */
            return map(mapper);
        }
        return (NOptional<V>) this;
    }

    @Override
    public <V> NOptional<V> mapIfNotBlank(Function<T, V> mapper) {
        if (isBlank()) {
            return NOptional.ofEmpty(message());
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotEmpty(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(message());
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotNull(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(message());
        }
        T v = get();
        if (v == null) {
            return NOptional.ofEmpty(message());
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotError(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(message());
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public NOptional<T> mapIf(Predicate<T> predicate, Function<T, T> trueExpr) {
        if (isEmpty() || isError()) {
            return this;
        }
        T t = get();
        boolean ok = predicate != null && predicate.test(t);
        return ok ? NOptional.of(trueExpr.apply(t)) : this;
    }

    @Override
    public <V> NOptional<V> mapIf(Predicate<T> predicate, Function<T, V> trueExpr, Function<T, V> falseExpr) {
        if (isEmpty() || isError()) {
            return NOptional.of(falseExpr.apply(null));
        }
        T t = get();
        boolean ok = predicate != null && predicate.test(t);
        return NOptional.of(ok ? trueExpr.apply(t) : falseExpr.apply(t));
    }

    /**
     * Checks if is default.
     *
     * @return is default result
     */
    public boolean isDefault() {
        if (isBlank()) {
            return true;
        }
        if (isNotPresent()) {
            return false;
        }
        T v = get();
        if (v == null) {
            return true;
        }
        if (v instanceof Boolean) {
            return !((Boolean) v).booleanValue();
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue() == 0;
        }
        return false;
    }

    @Override
    public NOptional<T> mapIfNotDefault(Function<T, T> mapper) {
        if (isDefault()) {
            return this;
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public NOptional<T> mapIfDefault(Function<T, T> mapper) {
        if (!isDefault()) {
            return this;
        }
        /**
         * Map.
         *
         * @param mapper mapper
         * @return map result
         */
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> map(Function<T, V> mapper) {
        NAssert.requireNamedNonNull(mapper);
        if (isPresent()) {
            return NOptional.of(mapper.apply(get()));
        }
        return (NOptional<V>) this;
    }

    /**
     * Then.
     *
     * @param mapper mapper
     * @return then result
     */
    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNamedNonNull(mapper);
        switch (type()) {
            case PRESENT: {
                try {
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
                } catch (Exception ex) {
                    return NOptional.ofError(message(), ex);
                }
            }
            case EMPTY: {
                return NOptional.ofEmpty(message());
            }
            case ERROR: {
                return NOptional.ofError(message(), getError());
            }
        }
        //never
        return NOptional.ofEmpty(message());
    }

    @Override
    public NOptional<T> filter(NMessagedPredicate<T> predicate) {
        NAssert.requireNamedNonNull(predicate);
        Predicate<T> filter = predicate.filter();
        NAssert.requireNamedNonNull(filter);
        if (isPresent()) {
            return filter.test(get()) ? this : NOptional.ofEmpty(predicate.message());
        }
        return this;
    }

    @Override
    public NOptional<T> filter(Predicate<T> predicate, Supplier<NMsg> message) {
        NAssert.requireNamedNonNull(predicate);
        if (isPresent()) {
            return predicate.test(get()) ? this : NOptional.ofEmpty(message);
        }
        return this;
    }

    @Override
    public NOptional<T> filter(Predicate<T> predicate) {
        /**
         * Filter.
         *
         * @param predicate predicate
         * @param null null
         * @return filter result
         */
        return filter(predicate, null);
    }

    @Override
    public NOptional<T> ifCondition(Predicate<NOptional<T>> condition, Consumer<NOptional<T>> action) {
        NAssert.requireNamedNonNull(action, "action");
        NAssert.requireNamedNonNull(condition, "condition");
        if (condition.test(this)) {
            action.accept(this);
        }
        return this;
    }

    @Override
    public NOptional<T> ifPresent(Consumer<T> action) {
        if (isPresent()) {
            NAssert.requireNamedNonNull(action);
            action.accept(get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifNonPresent(Runnable action) {
        if (isNotPresent()) {
            NAssert.requireNamedNonNull(action);
            action.run();
        }
        return this;
    }

    /**
     * If error.
     *
     * @param action action
     * @return if error result
     */
    public NOptional<T> ifError(Consumer<Throwable> action) {
        if (isError()) {
            if (action != null) {
                action.accept(getError());
            }
        }
        return this;
    }

    /**
     * If null.
     *
     * @param action action
     * @return if null result
     */
    public NOptional<T> ifNull(Runnable action) {
        if (isNull()) {
            if (action != null) {
                action.run();
            }
        }
        return this;
    }

    @Override
    public <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R {
        if (isPresent()) {
            /**
             * Returns the get.
             *
             * @return get result
             */
            return get();
        } else {
            throw NAssert.requireNamedNonNull(NAssert.requireNamedNonNull(exceptionSupplier).get());
        }
    }

    @Override
    public NOptional<T> orElseGetOptionalFrom(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            return NAssert.requireNamedNonNull(NAssert.requireNamedNonNull(other).get());
        }
        return this;
    }

    @Override
    public T orElse(T other) {
        if (isNotPresent()) {
            return other;
        }
        /**
         * Returns the get.
         *
         * @return get result
         */
        return get();
    }

    /**
     * Or else get optional of.
     *
     * @param other other
     * @return or else get optional of result
     */
    public NOptional<T> orElseGetOptionalOf(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.of(NAssert.requireNamedNonNull(other).get(), message());
        }
        return this;
    }

    @Override
    public T orElseGet(Supplier<? extends T> other) {
        if (isNotPresent()) {
            NAssert.requireNamedNonNull(other);
            return other.get();
        }
        /**
         * Returns the get.
         *
         * @return get result
         */
        return get();
    }

    @Override
    public NOptional<T> onBlankUse(Supplier<NOptional<T>> other) {
        if (isBlank()) {
            NAssert.requireNamedNonNull(other);
            return NAssert.requireNamedNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onNullUse(Supplier<NOptional<T>> other) {
        if (isNull()) {
            NAssert.requireNamedNonNull(other);
            T v = get();
            if (v == null) {
                return NAssert.requireNamedNonNull(other.get());
            }
        }
        return this;
    }

    @Override
    public NOptional<T> onNullEmpty() {
        if (isNull()) {
            return NOptional.ofEmpty(message());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyUse(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            NAssert.requireNamedNonNull(other);
            return NAssert.requireNamedNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onErrorUse(Supplier<NOptional<T>> other) {
        if (isError()) {
            NAssert.requireNamedNonNull(other);
            return NAssert.requireNamedNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onBlank(T other) {
        if (isBlank()) {
            return NOptional.of(other, message());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyNull() {
        if (isEmpty()) {
            return NOptional.ofNullable(null, message());
        }
        return this;
    }

    @Override
    public NOptional<T> onErrorEmpty() {
        /**
         * On error.
         *
         * @param null null
         * @return on error result
         */
        return onError(null);
    }

    @Override
    public NOptional<T> onEmpty(T other) {
        if (isEmpty()) {
            return NOptional.of(other, message());
        }
        return this;
    }

    @Override
    public NOptional<T> onError(T other) {
        if (isError()) {
            return NOptional.of(other, message());
        }
        return this;
    }

    @Override
    public T orNull() {
        /**
         * Or else.
         *
         * @param null null
         * @return or else result
         */
        return orElse(null);
    }

    @Override
    public boolean isNotPresent() {
        return !isPresent();
    }

    @Override
    public NOptional<T> withExceptionFactory(NOptionalExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
        return this;
    }

    @Override
    public NOptional<T> onBlankEmpty(Supplier<NMsg> emptyMessage) {
        if (isPresent()) {
            T v = get();
            if (NBlankable.isBlank(v)) {
                if (emptyMessage == null) {
                    emptyMessage = message();
                }
                return NOptional.ofEmpty(emptyMessage);
            }
        }
        return this;
    }

    @Override
    public NOptional<T> onBlankEmpty() {
        /**
         * On blank empty.
         *
         * @param null null
         * @return on blank empty result
         */
        return onBlankEmpty(null);
    }

    /**
     * Returns the exception factory.
     *
     * @return get exception factory result
     */
    public NOptionalExceptionFactory getExceptionFactory() {
        return exceptionFactory;
    }


}
