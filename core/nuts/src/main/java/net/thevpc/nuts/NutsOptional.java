package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsOptionalEmpty;
import net.thevpc.nuts.boot.PrivateNutsOptionalValid;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NutsOptional<T> {

    static <T> NutsOptional<T> ofEmpty(Function<NutsSession, NutsMessage> emptyMessage) {
        return new PrivateNutsOptionalEmpty<>(emptyMessage, false);
    }

    static <T> NutsOptional<T> ofError(Function<NutsSession, NutsMessage> errorMessage) {
        return new PrivateNutsOptionalEmpty<>(errorMessage, true);
    }

    static <T> NutsOptional<T> of(T value) {
        return of(value, null);
    }

    static <T> NutsOptional<T> ofNullable(T value) {
        return new PrivateNutsOptionalValid<>(value);
    }

    static <T> NutsOptional<T> of(T value, Function<NutsSession, NutsMessage> emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return new PrivateNutsOptionalValid<>(value);
    }

    static <T> NutsOptional<T> ofNull() {
        return new PrivateNutsOptionalValid<>(null);
    }

    static <T> NutsOptional<T> ofOptional(Optional<T> optional, Function<NutsSession, NutsMessage> errorMessage) {
        if (optional.isPresent()) {
            return of(optional.get());
        }
        return ofEmpty(errorMessage);
    }

    <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper);

    <V> NutsOptional<V> map(Function<T, V> mapper);

    <V> NutsOptional<V> filter(NutsMessagedPredicate<T> p);

    <V> NutsOptional<V> filter(Predicate<T> filter, Function<NutsSession, NutsMessage> message);

    NutsOptional<T> ifPresent(Consumer<T> t);

    <V> NutsOptional<V> asEmpty();

    T get();

    T get(NutsMessage message, NutsSession session);

    T get(NutsSession session);

    T orElse(T other);

    T orNull();

    NutsOptional<T> ifEmpty(T other);

    NutsOptional<T> ifBlank(T other);

    NutsOptional<T> ifError(T other);

    NutsOptional<T> ifEmptyGet(Supplier<NutsOptional<T>> other);

    NutsOptional<T> ifBlankGet(Supplier<NutsOptional<T>> other);

    NutsOptional<T> ifErrorGet(Supplier<NutsOptional<T>> other);

    NutsOptional<T> orElseGetOptional(Supplier<NutsOptional<T>> other);

    T orElseGet(Supplier<? extends T> other);

    boolean isEmpty();

    boolean isError();

    boolean isPresent();
    boolean isNotPresent();

    NutsOptional<T> nonBlank(Function<NutsSession, NutsMessage> emptyMessage);

    NutsOptional<T> nonBlank();

    Function<NutsSession, NutsMessage> getMessage();


    interface NutsMessagedPredicate<T> {
        Predicate<T> filter();

        Function<NutsSession, NutsMessage> message();
    }
}
