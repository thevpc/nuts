package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedOptionalEmpty;
import net.thevpc.nuts.reserved.NReservedOptionalError;
import net.thevpc.nuts.reserved.NReservedOptionalValid;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NOptional<T> extends NBlankable, NSessionProvider {

    static <T> NOptional<T> ofNamedEmpty(String name) {
        return ofEmpty(s -> NMsg.ofC("missing %s", name));
    }

    static <T> NOptional<T> ofNamedError(String name) {
        return ofError(s -> NMsg.ofC("error evaluating %s", name));
    }

    static <T> NOptional<T> ofNamedError(String name, Throwable throwable) {
        return ofError(s -> NMsg.ofC("error evaluating %s", name), throwable);
    }

    static <T> NOptional<T> ofEmpty() {
        return ofEmpty(null);
    }

    static <T> NOptional<T> ofEmpty(Function<NSession, NMsg> emptyMessage) {
        return new NReservedOptionalEmpty<>(emptyMessage);
    }

    static <T> NOptional<T> ofError(Function<NSession, NMsg> errorMessage) {
        return ofError(errorMessage, null);
    }

    static <T> NOptional<T> ofError(Function<NSession, NMsg> errorMessage, Throwable throwable) {
        return new NReservedOptionalError<>(errorMessage, throwable);
    }


    static <T> NOptional<T> of(T value) {
        return of(value, null);
    }

    static <T> NOptional<T> ofNullable(T value) {
        return new NReservedOptionalValid<>(value);
    }

    static <T> NOptional<T> ofNamed(T value, String name) {
        return of(value, s -> NMsg.ofC("missing %s", name));
    }

    static <T> NOptional<T> of(T value, Function<NSession, NMsg> emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return new NReservedOptionalValid<>(value);
    }

    static <T> NOptional<T> ofNull() {
        return ofNullable(null);
    }

    static <T> NOptional<T> ofNamedOptional(Optional<T> optional, String name) {
        return ofOptional(optional, s -> NMsg.ofC("missing %s", name));
    }

    static <T> NOptional<T> ofOptional(Optional<T> optional, Function<NSession, NMsg> errorMessage) {
        if (optional.isPresent()) {
            return of(optional.get());
        }
        return ofEmpty(errorMessage);
    }

    static <T> NOptional<T> ofSingleton(Collection<T> collection) {
        return ofSingleton(collection, null, null);
    }

    static <T> NOptional<T> ofNamedFirst(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofFirst(collection,
                s -> NMsg.ofC("missing %s", name)
        );
    }

    static <T> NOptional<T> ofNamedSingleton(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofSingleton(collection,
                s -> NMsg.ofC("missing %s", name)
                ,
                s -> NMsg.ofC("too many elements %s>1 for %s", collection == null ? 0 : collection.size(), name));
    }

    static <T> NOptional<T> ofSingleton(Collection<T> collection, Function<NSession, NMsg> emptyMessage, Function<NSession, NMsg> errorMessage) {
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        if (collection.size() > 1) {
            if (errorMessage == null) {
                errorMessage = s -> NMsg.ofC("too many elements %s>1", collection.size());
            }
            return ofError(errorMessage);
        }
        for (T t : collection) {
            return of(t);
        }
        return ofEmpty(errorMessage);
    }

    static <T> NOptional<T> ofFirst(Collection<T> collection) {
        return ofFirst(collection, null);
    }

    static <T> NOptional<T> ofFirst(Collection<T> collection, Function<NSession, NMsg> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = s -> NMsg.ofPlain("missing element");
        }
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        for (T t : collection) {
            return of(t);
        }
        return ofEmpty(emptyMessage);
    }

    default NOptional<T> failFast() {
        return failFast(null);
    }

    default NOptional<T> failFast(NSession session) {
        if (isError()) {
            get(session);
        }
        return this;
    }

    <V> NOptional<V> flatMap(Function<T, NOptional<V>> mapper);

    NOptional<T> withDefault(Supplier<T> value);

    NOptional<T> withDefault(T value);

    NOptional<T> withoutDefault();

    <V> NOptional<V> mapIfPresent(Function<T, V> mapper);

    <V> NOptional<V> mapIfNotBlank(Function<T, V> mapper);

    <V> NOptional<V> mapIfNotEmpty(Function<T, V> mapper);

    <V> NOptional<V> mapIfNotNull(Function<T, V> mapper);

    <V> NOptional<V> mapIf(Predicate<T> predicate, Function<T, V> trueExpr, Function<T, V> falseExpr);

    NOptional<T> mapIf(Predicate<T> predicate, Function<T, T> trueExpr);

    NOptional<T> mapIfNotDefault(Function<T, T> mapper);

    NOptional<T> mapIfDefault(Function<T, T> mapper);

    <V> NOptional<V> mapIfNotError(Function<T, V> mapper);

    <V> NOptional<V> map(Function<T, V> mapper);

    NOptional<T> filter(NMessagedPredicate<T> predicate);

    NOptional<T> filter(Predicate<T> predicate);

    NOptional<T> filter(Predicate<T> predicate, Function<NSession, NMsg> message);

    NOptional<T> ifPresent(Consumer<T> t);

    T orElse(T other);

    T orElseGet(Supplier<? extends T> other);

    NOptional<T> orElseOf(Supplier<T> other);

    NOptional<T> orElseOfNullable(Supplier<T> other);

    T ifEmptyGet(Supplier<? extends T> other);

    NOptional<T> ifEmptyOf(Supplier<T> other);

    public NOptional<T> ifEmptyOfNullable(Supplier<T> other);


    NOptional<T> orElseUse(Supplier<NOptional<T>> other);

    <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R;

    NOptional<T> ifEmpty(T other);

    NOptional<T> ifEmptyUse(Supplier<NOptional<T>> other);

    T get();

    T get(Function<NSession, NMsg> message, NSession session);

    T get(Supplier<NMsg> message);

    Throwable getError();

    T get(NSession session);


    T orNull();

    T orDefault();

    NOptional<T> ifEmptyNull();

    NOptional<T> ifBlank(T other);

    NOptional<T> ifBlankEmpty(Function<NSession, NMsg> emptyMessage);

    NOptional<T> ifBlankEmpty();


    NOptional<T> ifErrorNull();

    NOptional<T> ifError(T other);

    NOptional<T> ifBlankUse(Supplier<NOptional<T>> other);

    NOptional<T> ifNullUse(Supplier<NOptional<T>> other);

    NOptional<T> ifNullEmpty();

    NOptional<T> ifErrorUse(Supplier<NOptional<T>> other);


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

    Function<NSession, NMsg> getMessage();

    /**
     * set default session or null
     *
     * @param session default session or null
     * @return {@code this} instance
     */
    NOptional<T> setSession(NSession session);


}
