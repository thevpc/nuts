package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.util.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NReservedOptionalImpl<T> implements NOptional<T>, Cloneable {
    private ExceptionFactory exceptionFactory;

    public NReservedOptionalImpl() {
    }

    @Override
    public <V> NOptional<V> thenOptional(Function<T, NOptional<V>> mapper) {
        if (isPresent()) {
            NOptional<V> u = mapper.apply(get());
            if (u == null) {
                return NOptional.ofEmpty(getMessage());
            }
            return u;
        }
        return NOptional.ofEmpty(getMessage());
    }

    @Override
    public <V> NOptional<V> instanceOf(Class<V> type) {
        NAssert.requireNonNull(type,"type");
        return map(a->{
            if(type.isInstance(a)){
                return (V)a;
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
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return Objects.requireNonNull(mapper.apply(get()));
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
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return NOptional.of(mapper.apply(get()));
        }
        return (NOptional<V>) this;
    }

    public <V> NOptional<V> then(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
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
        Objects.requireNonNull(predicate);
        Predicate<T> filter = predicate.filter();
        Objects.requireNonNull(filter);
        if (isPresent()) {
            return filter.test(get()) ? this : NOptional.ofEmpty(predicate.message());
        }
        return this;
    }

    @Override
    public NOptional<T> filter(Predicate<T> predicate, Supplier<NMsg> message) {
        Objects.requireNonNull(predicate);
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
    public NOptional<T> ifPresent(Consumer<T> t) {
        if (isPresent()) {
            Objects.requireNonNull(t);
            t.accept(get());
        }
        return this;
    }

    @Override
    public <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R {
        if (isPresent()) {
            return get();
        } else {
            throw Objects.requireNonNull(Objects.requireNonNull(exceptionSupplier).get());
        }
    }

    @Override
    public NOptional<T> orElseUse(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            return Objects.requireNonNull(Objects.requireNonNull(other).get());
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

    @Override
    public T orElseGet(Supplier<? extends T> other) {
        if (isNotPresent()) {
            Objects.requireNonNull(other);
            return other.get();
        }
        return get();
    }

    public NOptional<T> orElseOf(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.of(Objects.requireNonNull(other).get(), getMessage());
        }
        return this;
    }

    public NOptional<T> orElseOfNullable(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.ofNullable(Objects.requireNonNull(other).get());
        }
        return this;
    }

    @Override
    public T ifEmptyGet(Supplier<? extends T> other) {
        if (isNotPresent()) {
            Objects.requireNonNull(other);
            return other.get();
        }
        return get();
    }

    @Override
    public NOptional<T> ifEmptyOf(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.of(Objects.requireNonNull(other).get(), getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyOfNullable(Supplier<T> other) {
        if (isNotPresent()) {
            return NOptional.ofNullable(Objects.requireNonNull(other).get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifBlankEmpty(Supplier<NMsg> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = () -> NMsg.ofPlain("blank value");
        }
        if (isPresent()) {
            T v = get();
            if (NBlankable.isBlank(v)) {
                return NOptional.ofEmpty(emptyMessage);
            }
        }
        return this;
    }

    @Override
    public NOptional<T> ifBlankEmpty() {
        return ifBlankEmpty(null);
    }

    @Override
    public NOptional<T> ifBlankUse(Supplier<NOptional<T>> other) {
        if (isBlank()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifNullUse(Supplier<NOptional<T>> other) {
        if (isNull()) {
            Objects.requireNonNull(other);
            T v = get();
            if (v == null) {
                return Objects.requireNonNull(other.get());
            }
        }
        return this;
    }

    @Override
    public NOptional<T> ifNullEmpty() {
        if (isNull()) {
            return NOptional.ofEmpty(getMessage());
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyUse(Supplier<NOptional<T>> other) {
        if (isEmpty()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifErrorUse(Supplier<NOptional<T>> other) {
        if (isError()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NOptional<T> ifBlank(T other) {
        if (isBlank()) {
            return NOptional.ofNullable(other);
        }
        return this;
    }

    @Override
    public NOptional<T> ifEmptyNull() {
        return ifEmpty(null);
    }

    @Override
    public NOptional<T> ifErrorNull() {
        return ifError(null);
    }

    @Override
    public NOptional<T> ifEmpty(T other) {
        if (isEmpty()) {
            return NOptional.ofNullable(other);
        }
        return this;
    }

    @Override
    public NOptional<T> ifError(T other) {
        if (isError()) {
            return NOptional.ofNullable(other);
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

    public ExceptionFactory getExceptionFactory() {
        return exceptionFactory;
    }


}
