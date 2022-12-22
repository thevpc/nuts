package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NutsReservedOptionalEmpty;
import net.thevpc.nuts.reserved.NutsReservedOptionalError;
import net.thevpc.nuts.reserved.NutsReservedOptionalValid;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NutsOptional<T> extends NutsBlankable {

    static <T> NutsOptional<T> ofNamedEmpty(String name) {
        return ofEmpty(s -> NutsMessage.ofCstyle("missing %s", name));
    }

    static <T> NutsOptional<T> ofNamedError(String name) {
        return ofError(s -> NutsMessage.ofCstyle("error evaluating %s", name));
    }

    static <T> NutsOptional<T> ofNamedError(String name, Throwable throwable) {
        return ofError(s -> NutsMessage.ofCstyle("error evaluating %s", name), throwable);
    }

    static <T> NutsOptional<T> ofEmpty() {
        return ofEmpty(null);
    }

    static <T> NutsOptional<T> ofEmpty(Function<NutsSession, NutsMessage> emptyMessage) {
        return new NutsReservedOptionalEmpty<>(emptyMessage);
    }

    static <T> NutsOptional<T> ofError(Function<NutsSession, NutsMessage> errorMessage) {
        return ofError(errorMessage, null);
    }

    static <T> NutsOptional<T> ofError(Function<NutsSession, NutsMessage> errorMessage, Throwable throwable) {
        return new NutsReservedOptionalError<>(errorMessage, throwable);
    }


    static <T> NutsOptional<T> of(T value) {
        return of(value, null);
    }

    static <T> NutsOptional<T> ofNullable(T value) {
        return new NutsReservedOptionalValid<>(value);
    }

    static <T> NutsOptional<T> ofNamed(T value, String name) {
        return of(value, s -> NutsMessage.ofCstyle("missing %s", name));
    }

    static <T> NutsOptional<T> of(T value, Function<NutsSession, NutsMessage> emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return new NutsReservedOptionalValid<>(value);
    }

    static <T> NutsOptional<T> ofNull() {
        return ofNullable(null);
    }

    static <T> NutsOptional<T> ofNamedOptional(Optional<T> optional, String name) {
        return ofOptional(optional, s -> NutsMessage.ofCstyle("missing %s", name));
    }

    static <T> NutsOptional<T> ofOptional(Optional<T> optional, Function<NutsSession, NutsMessage> errorMessage) {
        if (optional.isPresent()) {
            return of(optional.get());
        }
        return ofEmpty(errorMessage);
    }

    static <T> NutsOptional<T> ofSingleton(Collection<T> collection) {
        return ofSingleton(collection, null, null);
    }

    static <T> NutsOptional<T> ofNamedFirst(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofFirst(collection,
                s -> NutsMessage.ofCstyle("missing %s", name)
        );
    }

    static <T> NutsOptional<T> ofNamedSingleton(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofSingleton(collection,
                s -> NutsMessage.ofCstyle("missing %s", name)
                ,
                s -> NutsMessage.ofCstyle("too many (%s>1) values for %s", collection == null ? 0 : collection.size(), name));
    }

    static <T> NutsOptional<T> ofSingleton(Collection<T> collection, Function<NutsSession, NutsMessage> emptyMessage, Function<NutsSession, NutsMessage> errorMessage) {
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        if (collection.size() > 1) {
            if (errorMessage == null) {
                errorMessage = s -> NutsMessage.ofCstyle("too many elements %s >1", collection.size());
            }
            return ofError(errorMessage);
        }
        for (T t : collection) {
            return of(t);
        }
        return ofEmpty(errorMessage);
    }

    static <T> NutsOptional<T> ofFirst(Collection<T> collection) {
        return ofFirst(collection, null);
    }

    static <T> NutsOptional<T> ofFirst(Collection<T> collection, Function<NutsSession, NutsMessage> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = s -> NutsMessage.ofPlain("missing element");
        }
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        for (T t : collection) {
            return of(t);
        }
        return ofEmpty(emptyMessage);
    }

    default NutsOptional<T> failFast() {
        return failFast(null);
    }

    default NutsOptional<T> failFast(NutsSession session) {
        if (isError()) {
            get(session);
        }
        return this;
    }

    <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper);

    NutsOptional<T> withDefault(Supplier<T> value);

    NutsOptional<T> withDefault(T value);

    NutsOptional<T> withoutDefault();

    <V> NutsOptional<V> mapIfPresent(Function<T, V> mapper);

    <V> NutsOptional<V> mapIfNotBlank(Function<T, V> mapper);

    <V> NutsOptional<V> mapIfNotEmpty(Function<T, V> mapper);

    <V> NutsOptional<V> mapIfNotNull(Function<T, V> mapper);

    <V> NutsOptional<V> mapIf(Predicate<T> predicate, Function<T, V> trueExpr, Function<T, V> falseExpr);

    NutsOptional<T> mapIf(Predicate<T> predicate, Function<T, T> trueExpr);

    NutsOptional<T> mapIfNotDefault(Function<T, T> mapper);

    NutsOptional<T> mapIfDefault(Function<T, T> mapper);

    <V> NutsOptional<V> mapIfNotError(Function<T, V> mapper);

    <V> NutsOptional<V> map(Function<T, V> mapper);

    NutsOptional<T> filter(NutsMessagedPredicate<T> predicate);

    NutsOptional<T> filter(Predicate<T> predicate);

    NutsOptional<T> filter(Predicate<T> predicate, Function<NutsSession, NutsMessage> message);

    NutsOptional<T> ifPresent(Consumer<T> t);

    T orElse(T other);

    T orElseGet(Supplier<? extends T> other);

    NutsOptional<T> orElseOf(Supplier<T> other);
    NutsOptional<T> orElseOfNullable(Supplier<T> other);

    NutsOptional<T> orElseUse(Supplier<NutsOptional<T>> other);

    <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R;

    NutsOptional<T> ifEmpty(T other);

    NutsOptional<T> ifEmptyUse(Supplier<NutsOptional<T>> other);

    T get();

    T get(Function<NutsSession, NutsMessage> message, NutsSession session);

    T get(Supplier<NutsMessage> message);

    Throwable getError();

    T get(NutsSession session);


    T orNull();

    T orDefault();

    NutsOptional<T> ifEmptyNull();

    NutsOptional<T> ifBlank(T other);

    NutsOptional<T> ifBlankEmpty(Function<NutsSession, NutsMessage> emptyMessage);

    NutsOptional<T> ifBlankEmpty();


    NutsOptional<T> ifErrorNull();

    NutsOptional<T> ifError(T other);

    NutsOptional<T> ifBlankUse(Supplier<NutsOptional<T>> other);

    NutsOptional<T> ifNullUse(Supplier<NutsOptional<T>> other);
    NutsOptional<T> ifNullEmpty();

    NutsOptional<T> ifErrorUse(Supplier<NutsOptional<T>> other);


    /**
     * return true when not an error and has no content. {@code isPresent()} would return false as well.
     *
     * @return true when not an error and has no content
     */
    boolean isEmpty();

    /**
     * return true if this is valid null value. {@code isPresent()} would return true as well.
     *
     * @return true if this is valid null value
     */
    boolean isNull();

    /**
     * return true if this is an error value. {@code isPresent()} would return false as well. {@code isEmpty()} would return false.
     *
     * @return true if this is an error value
     */
    boolean isError();

    /**
     * return true if this is neither error nor empty value.
     *
     * @return true if this is neither error nor empty value
     */
    boolean isPresent();

    /**
     * return true if this is either error or empty value.
     *
     * @return true if this is either error or empty value
     */
    boolean isNotPresent();

    Function<NutsSession, NutsMessage> getMessage();

    /**
     * set default session or null
     *
     * @param session default session or null
     * @return {@code this} instance
     */
    NutsOptional<T> setSession(NutsSession session);

    /**
     * return default session or null
     *
     * @return default session or null
     */
    NutsSession getSession();


}
