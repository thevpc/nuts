package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NReservedOptionalImpl<T> implements NOptional<T>, Cloneable {
    private ExceptionFactory exceptionFactory;
    private Supplier<NMsg> message;

    public NReservedOptionalImpl(Supplier<NMsg> message) {
        if (message == null) {
            message = NMsg::ofMissingValue;
        }
        this.message = message;
    }

    @Override
    public Supplier<NMsg> getMessage() {
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

    public T get() {
        return get(null);
    }

    public <V> NOptional<V> flatMap(Function<T, NOptional<V>> mapper) {
        NAssert.requireNonNull(mapper);
        if (isPresent()) {
            NOptional<V> b = mapper.apply(get());
            if (b == null) {
                return NOptional.ofEmpty(getMessage());
            }
            return b;
        }
        return (NOptional<V>) this;
    }

    @Override
    public <V> NOptional<V> mapIfPresent(Function<T, V> mapper) {
        if (isPresent()) {
            return map(mapper);
        }
        return (NOptional<V>) this;
    }

    @Override
    public <V> NOptional<V> mapIfNotBlank(Function<T, V> mapper) {
        if (isBlank()) {
            return NOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotEmpty(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotNull(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(getMessage());
        }
        T v = get();
        if (v == null) {
            return NOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> mapIfNotError(Function<T, V> mapper) {
        if (isEmpty()) {
            return NOptional.ofEmpty(getMessage());
        }
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
        return map(mapper);
    }

    @Override
    public NOptional<T> mapIfDefault(Function<T, T> mapper) {
        if (!isDefault()) {
            return this;
        }
        return map(mapper);
    }

    @Override
    public <V> NOptional<V> map(Function<T, V> mapper) {
        NAssert.requireNonNull(mapper);
        if (isPresent()) {
            return NOptional.of(mapper.apply(get()));
        }
        return (NOptional<V>) this;
    }

    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNonNull(mapper);
        switch (getType()) {
            case PRESENT: {
                try {
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
                } catch (Exception ex) {
                    return NOptional.ofError(getMessage(), ex);
                }
            }
            case EMPTY: {
                return NOptional.ofEmpty(getMessage());
            }
            case ERROR: {
                return NOptional.ofError(getMessage(), getError());
            }
        }
        //never
        return NOptional.ofEmpty(getMessage());
    }

    @Override
    public NOptional<T> filter(NMessagedPredicate<T> predicate) {
        NAssert.requireNonNull(predicate);
        Predicate<T> filter = predicate.filter();
        NAssert.requireNonNull(filter);
        if (isPresent()) {
            return filter.test(get()) ? this : NOptional.ofEmpty(predicate.message());
        }
        return this;
    }

    @Override
    public NOptional<T> filter(Predicate<T> predicate, Supplier<NMsg> message) {
        NAssert.requireNonNull(predicate);
        if (isPresent()) {
            return predicate.test(get()) ? this : NOptional.ofEmpty(message);
        }
        return this;
    }

    @Override
    public NOptional<T> filter(Predicate<T> predicate) {
        return filter(predicate, (Supplier<NMsg>) null);
    }

    @Override
    public NOptional<T> ifCondition(Predicate<NOptional<T>> condition, Consumer<NOptional<T>> action) {
        NAssert.requireNonNull(action, "action");
        NAssert.requireNonNull(condition, "condition");
        if (condition.test(this)) {
            action.accept(this);
        }
        return this;
    }

    @Override
    public NOptional<T> ifPresent(Consumer<T> action) {
        if (isPresent()) {
            NAssert.requireNonNull(action);
            action.accept(get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifNonPresent(Runnable action) {
        if (isNotPresent()) {
            NAssert.requireNonNull(action);
            action.run();
        }
        return this;
    }

    public NOptional<T> ifError(Consumer<Throwable> action) {
        if (isError()) {
            if (action != null) {
                action.accept(getError());
            }
        }
        return this;
    }

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
            return get();
        } else {
            throw NAssert.requireNonNull(NAssert.requireNonNull(exceptionSupplier).get());
        }
    }

    @Override
    public NOptional<T> orElseGetOptionalFrom(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            return NAssert.requireNonNull(NAssert.requireNonNull(other).get());
        }
        return this;
    }

    @Override
    public T orElse(T other) {
        if (isNotPresent()) {
            return other;
        }
        return get();
    }

    public NOptional<T> orElseGetOptionalOf(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.of(NAssert.requireNonNull(other).get(), getMessage());
        }
        return this;
    }

    @Override
    public T orElseGet(Supplier<? extends T> other) {
        if (isNotPresent()) {
            NAssert.requireNonNull(other);
            return other.get();
        }
        return get();
    }

    @Override
    public NOptional<T> onBlankUse(Supplier<NOptional<T>> other) {
        if (isBlank()) {
            NAssert.requireNonNull(other);
            return NAssert.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onNullUse(Supplier<NOptional<T>> other) {
        if (isNull()) {
            NAssert.requireNonNull(other);
            T v = get();
            if (v == null) {
                return NAssert.requireNonNull(other.get());
            }
        }
        return this;
    }

    @Override
    public NOptional<T> onNullEmpty() {
        if (isNull()) {
            return NOptional.ofEmpty(getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyUse(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            NAssert.requireNonNull(other);
            return NAssert.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onErrorUse(Supplier<NOptional<T>> other) {
        if (isError()) {
            NAssert.requireNonNull(other);
            return NAssert.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> onBlank(T other) {
        if (isBlank()) {
            return NOptional.of(other, getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyNull() {
        if (isEmpty()) {
            return NOptional.ofNullable(null, getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> onErrorEmpty() {
        return onError(null);
    }

    @Override
    public NOptional<T> onEmpty(T other) {
        if (isEmpty()) {
            return NOptional.of(other, getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> onError(T other) {
        if (isError()) {
            return NOptional.of(other, getMessage());
        }
        return this;
    }

    @Override
    public T orNull() {
        return orElse(null);
    }

    @Override
    public boolean isNotPresent() {
        return !isPresent();
    }

    @Override
    public NOptional<T> withExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
        return this;
    }

    @Override
    public NOptional<T> onBlankEmpty(Supplier<NMsg> emptyMessage) {
        if (isPresent()) {
            T v = get();
            if (NBlankable.isBlank(v)) {
                if (emptyMessage == null) {
                    emptyMessage = getMessage();
                }
                return NOptional.ofEmpty(emptyMessage);
            }
        }
        return this;
    }

    @Override
    public NOptional<T> onBlankEmpty() {
        return onBlankEmpty(null);
    }

    public ExceptionFactory getExceptionFactory() {
        return exceptionFactory;
    }


}
