package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NutsReservedOptionalImpl<T> implements NutsOptional<T> {

    public NutsReservedOptionalImpl() {
    }

    public T get() {
        return get(null, null);
    }

    static NutsMessage buildMessage(NutsSession session, Function<NutsSession, NutsMessage> message0, NutsMessage message) {
        NutsMessage m = message0.apply(session);
        return m.concat(session, NutsMessage.plain(" : "), message);
    }

    public <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return Objects.requireNonNull(mapper.apply(get()));
        }
        return (NutsOptional) this;
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

    public NutsOptional<T> ifPresent(Consumer<T> t) {
        if (isPresent()) {
            t.accept(get());
        }
        return this;
    }

    public <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }


    @Override
    public NutsOptional<T> orElseGetOptional(Supplier<NutsOptional<T>> other) {
        if (isNotPresent()) {
            return other.get();
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
            return other.get();
        }
        return get();
    }

    @Override
    public NutsOptional<T> nonBlank(Function<NutsSession, NutsMessage> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = session -> NutsMessage.cstyle("blank value");
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
    public NutsOptional<T> nonBlank() {
        return nonBlank(null);
    }

    @Override
    public NutsOptional<T> ifBlankGet(Supplier<NutsOptional<T>> other) {
        if (isPresent()) {
            T v = get();
            if (NutsBlankable.isBlank(v)) {
                return other.get();
            }
        } else if (isEmpty()) {
            return other.get();
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifEmptyGet(Supplier<NutsOptional<T>> other) {
        if (isEmpty()) {
            return other.get();
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifErrorGet(Supplier<NutsOptional<T>> other) {
        if (isError()) {
            return other.get();
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifBlank(T other) {
        if (isPresent()) {
            T v = get();
            if (NutsBlankable.isBlank(v)) {
                return NutsOptional.of(other);
            }
        } else if (isEmpty()) {
            return NutsOptional.of(other);
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
        if (!isError() && isEmpty()) {
            return new NutsReservedOptionalValid<>(other);
        }
        return this;
    }

    @Override
    public NutsOptional<T> ifError(T other) {
        if (isError()) {
            return new NutsReservedOptionalValid<>(other);
        }
        return this;
    }

    @Override
    public T orNull() {
        return orNull();
    }

    @Override
    public boolean isNotPresent() {
        return !isPresent();
    }

}
