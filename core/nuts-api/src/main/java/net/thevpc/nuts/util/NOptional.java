package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.optional.NReservedOptionalEmpty;
import net.thevpc.nuts.reserved.optional.NReservedOptionalError;
import net.thevpc.nuts.reserved.optional.NReservedOptionalValidCallable;
import net.thevpc.nuts.reserved.optional.NReservedOptionalValidValue;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NOptional<T> extends NBlankable {

    static <T> NOptional<T> ofNamedEmpty(String name) {
        return ofEmpty(() -> NMsg.ofC("missing %s", NStringUtils.firstNonBlank(name, "value")));
    }

    static <T> NOptional<T> ofNamedEmpty(NMsg message) {
        return ofEmpty(NMsg.ofC("missing %s", message==null?"value":message));
    }

    static <T> NOptional<T> ofNamedError(NMsg message) {
        return ofError(message == null ? () -> NMsg.ofC("error evaluating %s", "value") : () -> message);
    }

    static <T> NOptional<T> ofNamedError(NMsg message, Throwable throwable) {
        return ofError(message == null ? () -> NMsg.ofC("error evaluating %s", "value") : () -> message, throwable);
    }

    static <T> NOptional<T> ofNamedError(String name) {
        return ofError(() -> NMsg.ofC("error evaluating %s", NStringUtils.firstNonBlank(name, "value")));
    }

    static <T> NOptional<T> ofNamedError(String name, Throwable throwable) {
        return ofError(() -> NMsg.ofC("error evaluating %s", name), throwable);
    }

    static <T> NOptional<T> ofEmpty() {
        return ofEmpty((Supplier<NMsg>)null);
    }

    static <T> NOptional<T> ofEmpty(Supplier<NMsg> emptyMessage) {
        return new NReservedOptionalEmpty<>(emptyMessage);
    }

    static <T> NOptional<T> ofEmpty(NMsg emptyMessage) {
        return new NReservedOptionalEmpty<>(()->emptyMessage);
    }

    static <T> NOptional<T> ofError(Supplier<NMsg> errorMessage) {
        return ofError(errorMessage, null);
    }

    static <T> NOptional<T> ofError(NMsg errorMessage) {
        return ofError(errorMessage==null?null:()->errorMessage, null);
    }

    static <T> NOptional<T> ofError(Supplier<NMsg> errorMessage, Throwable throwable) {
        return new NReservedOptionalError<>(errorMessage, throwable);
    }

    static <T> NOptional<T> of(T value) {
        return of(value, (Supplier<NMsg>) null);
    }

    static <T> NOptional<T> ofNullable(T value) {
        return new NReservedOptionalValidValue<>(value);
    }

    static <T> NOptional<T> ofCallable(NCallable<T> value) {
        NAssert.requireNonNull(value, "callable");
        return new NReservedOptionalValidCallable<>(value);
    }

    static <T> NOptional<T> ofSupplier(Supplier<T> value) {
        NAssert.requireNonNull(value, "supplier");
        return new NReservedOptionalValidCallable<>(() -> value.get());
    }

    static <T> NOptional<T> ofNamed(T value, String name) {
        return of(value, () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlank(name, "value")));
    }

    static <T> NOptional<T> of(T value, Supplier<NMsg> emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return ofNullable(value);
    }

    static <T> NOptional<T> of(T value, NMsg emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return ofNullable(value);
    }

    static <T> NOptional<T> ofNull() {
        return ofNullable(null);
    }

    static <T> NOptional<T> ofNamedOptional(Optional<T> optional, String name) {
        return ofOptional(optional, () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlank(name, "value")));
    }

    static <T> NOptional<T> ofOptional(Optional<T> optional, NMsg name) {
        return ofOptional(optional, () -> name);
    }

    static <T> NOptional<T> ofOptional(Optional<T> optional, Supplier<NMsg> errorMessage) {
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
                () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlank(name, "value"))
        );
    }

    static <T> NOptional<T> ofNamedSingleton(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofSingleton(collection,
                () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlank(name, "value")),
                () -> NMsg.ofC("too many elements %s>1 for %s", collection == null ? 0 : collection.size(), NStringUtils.firstNonBlank(name, "value")));
    }

    static <T> NOptional<T> ofSingleton(Collection<T> collection, Supplier<NMsg> emptyMessage, Supplier<NMsg> errorMessage) {
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        if (collection.size() > 1) {
            if (errorMessage == null) {
                errorMessage = () -> NMsg.ofC("too many elements %s>1", collection.size());
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

    static <T> NOptional<T> ofFirst(Collection<T> collection, Supplier<NMsg> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = () -> NMsg.ofPlain("missing element");
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
        if (isError()) {
            get();
        }
        return this;
    }

    <V> NOptional<V> flatMap(Function<T, NOptional<V>> mapper);

    NOptional<T> withDefault(Supplier<T> value);

    NOptional<T> withDefaultOptional(Supplier<NOptional<T>> value);

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
    
    /**
     * handy method to 'denull' expressions and handle things like <code>a?.b()?.c</code>
     * That is not possible in the Java Programming Language.
     * the equivalent code would be :
     * <pre>
     * NOptional.of(a).then(x->x.b()).then(x->x.c).get()
     * </pre>
     * @param <V> final result
     * @param mapper function to apply
     * @return null if this optional is null or empty otherwise, maps using mapper
     */
    <V> NOptional<V> then(Function<T, V> mapper);

    NOptional<T> filter(NMessagedPredicate<T> predicate);

    NOptional<T> filter(Predicate<T> predicate);

    NOptional<T> filter(Predicate<T> predicate, Supplier<NMsg> message);

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


    T get(Supplier<NMsg> message);

    Throwable getError();

    ExceptionFactory getExceptionFactory();

    T orNull();

    T orDefault();

    NOptional<T> orDefaultOptional();

    NOptional<T> ifEmptyNull();

    NOptional<T> ifBlank(T other);

    NOptional<T> ifBlankEmpty(Supplier<NMsg> emptyMessage);

    NOptional<T> ifBlankEmpty();

    NOptional<T> ifErrorNull();

    NOptional<T> ifError(T other);

    NOptional<T> ifBlankUse(Supplier<NOptional<T>> other);

    NOptional<T> ifNullUse(Supplier<NOptional<T>> other);

    NOptional<T> ifNullEmpty();

    NOptional<T> ifErrorUse(Supplier<NOptional<T>> other);

    /**
     * return true when not an error and has no content. {@code isPresent()}
     * would return false as well.
     *
     * @return true when not an error and has no content
     */
    boolean isEmpty();

    /**
     * return true if this is valid null value. {@code isPresent()} would return
     * true as well.
     *
     * @return true if this is valid null value
     */
    boolean isNull();

    /**
     * return true if this is an error value. {@code isPresent()} would return
     * false as well. {@code isEmpty()} would return false.
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
    
    NOptionalType getType();

    Supplier<NMsg> getMessage();

    NOptional<T> withExceptionFactory(ExceptionFactory exceptionFactory);

    interface ExceptionFactory{
        RuntimeException createException(NMsg message, Throwable e);
    }
}
