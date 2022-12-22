package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NutsReservedOptionalImpl<T> implements NutsOptional<T> {
    private NutsSession session;

    public NutsReservedOptionalImpl() {
    }

    public T get() {
        return get(null, null);
    }

    public <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return Objects.requireNonNull(mapper.apply(get()));
        }
        return (NutsOptional) this;
    }

    @Override
    public <V> NutsOptional<V> mapIfPresent(Function<T, V> mapper) {
        if (isNotPresent()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NutsOptional<V> mapIfNotBlank(Function<T, V> mapper) {
        if (isBlank()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NutsOptional<V> mapIfNotEmpty(Function<T, V> mapper) {
        if (isEmpty()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NutsOptional<V> mapIfNotNull(Function<T, V> mapper) {
        if (isEmpty()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        T v = get();
        if (v == null) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public <V> NutsOptional<V> mapIfNotError(Function<T, V> mapper) {
        if (isEmpty()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return map(mapper);
    }

    @Override
    public NutsOptional<T> mapIf(Predicate<T> predicate, Function<T, T> trueExpr) {
        if (isEmpty() || isError()) {
            return this;
        }
        T t = get();
        boolean ok = predicate != null && predicate.test(t);
        return ok ? NutsOptional.of(trueExpr.apply(t)) : this;
    }

    @Override
    public <V> NutsOptional<V> mapIf(Predicate<T> predicate, Function<T, V> trueExpr, Function<T, V> falseExpr) {
        if (isEmpty() || isError()) {
            return NutsOptional.of(falseExpr.apply(null));
        }
        T t = get();
        boolean ok = predicate != null && predicate.test(t);
        return NutsOptional.of(ok ? trueExpr.apply(t) : falseExpr.apply(t));
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
    public NutsOptional<T> mapIfNotDefault(Function<T, T> mapper) {
        if (isDefault()) {
            return this;
        }
        return map(mapper);
    }

    @Override
    public NutsOptional<T> mapIfDefault(Function<T, T> mapper) {
        if (!isDefault()) {
            return this;
        }
        return map(mapper);
    }

    public <V> NutsOptional<V> map(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return NutsOptional.of(mapper.apply(get()));
        }
        return (NutsOptional) this;
    }

    public NutsOptional<T> filter(NutsMessagedPredicate<T> predicate) {
        Objects.requireNonNull(predicate);
        Predicate<T> filter = predicate.filter();
        Objects.requireNonNull(filter);
        if (isPresent()) {
            return filter.test(get()) ? this : NutsOptional.ofEmpty(predicate.message());
        }
        return this;
    }

    public NutsOptional<T> filter(Predicate<T> predicate, Function<NutsSession, NutsMessage> message) {
        Objects.requireNonNull(predicate);
        if (isPresent()) {
            return predicate.test(get()) ? this : NutsOptional.ofEmpty(message);
        }
        return this;
    }

    @Override
    public NutsOptional<T> filter(Predicate<T> predicate) {
        return filter(predicate, (Function<NutsSession, NutsMessage>) null);
    }

    public NutsOptional<T> ifPresent(Consumer<T> t) {
        if (isPresent()) {
            Objects.requireNonNull(t);
            t.accept(get());
        }
        return this;
    }

    public <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R {
        if (isPresent()) {
            return get();
        } else {
            throw Objects.requireNonNull(Objects.requireNonNull(exceptionSupplier).get());
        }
    }


    @Override
    public NutsOptional<T> orElseUse(Supplier<NutsOptional<T>> other) {
        if (isNotPresent()) {
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

    public NutsOptional<T> orElseOf(Supplier<T> other) {
        if (isNotPresent()) {
            return NutsOptional.of(Objects.requireNonNull(other).get(), getMessage());
        }
        return this;
    }

    public NutsOptional<T> orElseOfNullable(Supplier<T> other) {
        if (isNotPresent()) {
            return NutsOptional.ofNullable(Objects.requireNonNull(other).get());
        }
        return this;
    }


    @Override
    public NutsOptional<T> ifBlankEmpty(Function<NutsSession, NutsMessage> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = session -> NutsMessage.ofPlain("blank value");
        }
        if (isPresent()) {
            T v = get();
            if (NutsBlankable.isBlank(v)) {
                return NutsOptional.ofEmpty(emptyMessage);
            }
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifBlankEmpty() {
        return ifBlankEmpty(null);
    }

    @Override
    public NutsOptional<T> ifBlankUse(Supplier<NutsOptional<T>> other) {
        if (isBlank()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifNullUse(Supplier<NutsOptional<T>> other) {
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
    public NutsOptional<T> ifNullEmpty() {
        if (isNull()) {
            return NutsOptional.ofEmpty(getMessage());
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifEmptyUse(Supplier<NutsOptional<T>> other) {
        if (isEmpty()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifErrorUse(Supplier<NutsOptional<T>> other) {
        if (isError()) {
            Objects.requireNonNull(other);
            return Objects.requireNonNull(other.get());
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifBlank(T other) {
        if (isBlank()) {
            return NutsOptional.ofNullable(other);
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifEmptyNull() {
        return ifEmpty(null);
    }

    @Override
    public NutsOptional<T> ifErrorNull() {
        return ifError(null);
    }

    @Override
    public NutsOptional<T> ifEmpty(T other) {
        if (isEmpty()) {
            return NutsOptional.ofNullable(other).setSession(getSession());
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifError(T other) {
        if (isError()) {
            return NutsOptional.ofNullable(other).setSession(getSession());
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

    public NutsSession getSession() {
        return session;
    }

    public NutsOptional<T> setSession(NutsSession session) {
        this.session = session;
        return this;
    }
}
