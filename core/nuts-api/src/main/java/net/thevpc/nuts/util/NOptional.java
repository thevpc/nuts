package net.thevpc.nuts.util;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.internal.optional.NReservedOptionalEmpty;
import net.thevpc.nuts.internal.optional.NReservedOptionalError;
import net.thevpc.nuts.internal.optional.NReservedOptionalValidCallable;
import net.thevpc.nuts.internal.optional.NReservedOptionalValidValue;
import net.thevpc.nuts.text.NMsg;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NOptional<T> extends NBlankable, NElementDescribable {

    /**
     * return the default ExceptionFactory used for generating exceptions thrown
     * when NOptional::get fails. When null default implementation falls back to
     * one of NEmptyOptionalException, NErrorOptionalException (when in
     * NWorkspace context) NDetachedEmptyOptionalException,
     * NDetachedErrorOptionalException (when no NWorkspace context can be
     * resolved)
     *
     * @return default ExceptionFactory
     */
    static ExceptionFactory getDefaultExceptionFactory() {
        return NExceptions.getDefaultExceptionFactory();
    }

    /**
     * set the default ExceptionFactory used for generating exceptions thrown
     * when NOptional::get fails. When null default implementation falls back to
     * one of NEmptyOptionalException, NErrorOptionalException (when in
     * NWorkspace context) NDetachedEmptyOptionalException,
     * NDetachedErrorOptionalException (when no NWorkspace context can be
     * resolved)
     *
     * @return default ExceptionFactory
     */
    static void setDefaultExceptionFactory(ExceptionFactory defaultExceptionFactory) {
        NExceptions.setDefaultExceptionFactory(defaultExceptionFactory);
    }

    static <T> NOptional<T> ofNamedEmpty(String name) {
        return ofEmpty(() -> NMsg.ofC("missing %s", NStringUtils.firstNonBlankTrimmed(name, "value")));
    }

    static <T> NOptional<T> ofNamedEmpty(NMsg message) {
        return ofEmpty(NMsg.ofC("missing %s", message == null ? "value" : message));
    }

    static <T> NOptional<T> ofNamedError(NMsg message) {
        return ofError(message == null ? () -> NMsg.ofC("error evaluating %s", "value") : () -> message);
    }

    static <T> NOptional<T> ofNamedError(NMsg message, Throwable throwable) {
        return ofError(message == null ? () -> NMsg.ofC("error evaluating %s", "value") : () -> message, throwable);
    }

    static <T> NOptional<T> ofNamedError(String name) {
        return ofError(() -> NMsg.ofC("error evaluating %s", NStringUtils.firstNonBlankTrimmed(name, "value")));
    }

    static <T> NOptional<T> ofNamedError(String name, Throwable throwable) {
        return ofError(() -> NMsg.ofC("error evaluating %s", name), throwable);
    }

    static <T> NOptional<T> ofEmpty() {
        return ofEmpty((Supplier<NMsg>) null);
    }

    static <T> NOptional<T> ofEmpty(Supplier<NMsg> emptyMessage) {
        return new NReservedOptionalEmpty<>(emptyMessage);
    }

    static <T> NOptional<T> ofEmpty(NMsg emptyMessage) {
        return new NReservedOptionalEmpty<>(() -> emptyMessage);
    }

    static <T> NOptional<T> ofError(Supplier<NMsg> errorMessage) {
        return ofError(errorMessage, null);
    }

    static <T> NOptional<T> ofError(NMsg errorMessage) {
        return ofError(errorMessage == null ? null : () -> errorMessage, null);
    }

    static <T> NOptional<T> ofError(NMsg errorMessage, Throwable throwable) {
        return ofError(errorMessage == null ? null : () -> errorMessage, throwable);
    }

    static <T> NOptional<T> ofError(Supplier<NMsg> errorMessage, Throwable throwable) {
        return new NReservedOptionalError<>(errorMessage, throwable);
    }

    static <T> NOptional<T> ofError(Throwable throwable) {
        return new NReservedOptionalError<>(null, throwable);
    }

    static <T> NOptional<T> of(T value) {
        return of(value, (Supplier<NMsg>) null);
    }

    static <T> NOptional<T> ofNullable(T value) {
        return ofNullable(value, (Supplier<NMsg>) null);
    }

    static <T> NOptional<T> ofNullable(T value, Supplier<NMsg> message) {
        return new NReservedOptionalValidValue<>(value, message);
    }

    static <T> NOptional<T> ofCallable(NCallable<T> value) {
        NAssert.requireNonNull(value, "callable");
        return new NReservedOptionalValidCallable<>(() -> NOptional.of(value.call()), null);
    }

    static <T> NOptional<T> ofSupplier(Supplier<T> value) {
        NAssert.requireNonNull(value, "supplier");
        return new NReservedOptionalValidCallable<>(() -> NOptional.of(value.get()), null);
    }

    static <T> NOptional<T> ofNamed(T value, String name) {
        return of(value, () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlankTrimmed(name, "value")));
    }

    static <T> NOptional<T> ofNamed(T value, NMsg name) {
        return of(value, () -> NMsg.ofC("missing %s", name == null ? "value" : name));
    }

    static <T> NOptional<T> of(T value, Supplier<NMsg> emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return ofNullable(value, emptyMessage);
    }

    static <T> NOptional<T> of(T value, NMsg emptyMessage) {
        if (value == null) {
            return ofEmpty(emptyMessage);
        }
        return ofNullable(value, emptyMessage == null ? null : () -> emptyMessage);
    }

    static <T> NOptional<T> ofNull() {
        return ofNullable(null);
    }

    static <T> NOptional<T> ofNamedOptional(Optional<T> optional, String name) {
        return ofOptional(optional, () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlankTrimmed(name, "value")));
    }

    static <T> NOptional<T> ofOptional(Optional<T> optional, NMsg errorMessage) {
        return ofOptional(optional, errorMessage == null ? null : () -> errorMessage);
    }

    static <T> NOptional<T> ofOptional(Optional<T> optional, Supplier<NMsg> errorMessage) {
        if (optional.isPresent()) {
            return of(optional.get(), errorMessage);
        }
        return ofEmpty(errorMessage);
    }

    /**
     * Creates an NOptional from a collection that must contain exactly one element.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has exactly 1 element: returns NOptional wrapping that element
     * - Has more than 1 element: returns error optional with message "too many elements"
     * <p>
     * Use this when you expect exactly one result, and want to catch duplicates as errors.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.findByEmail("user@example.com");
     *   NOptional<User> user = NOptional.ofSingleton(results);
     *   // Empty if no results, User if exactly 1, Error if >1
     * </pre>
     *
     * @param collection the collection to extract from
     * @return NOptional with the single element, or empty/error based on collection size
     */
    static <T> NOptional<T> ofSingleton(Collection<T> collection) {
        return ofSingleton(collection, null, null);
    }


    /**
     * Creates an NOptional from a collection that must contain exactly one element.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has exactly 1 element: returns NOptional wrapping that element
     * - Has more than 1 element: returns error optional with message "too many elements"
     * <p>
     * Use this when you expect exactly one result, and want to catch duplicates as errors.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.findByEmail("user@example.com");
     *   NOptional<User> user = NOptional.ofSingleton(results);
     *   // Empty if no results, User if exactly 1, Error if >1
     * </pre>
     *
     * @param collection the collection to extract from
     * @param name       name a appear in the error message as "missing $name"
     * @return NOptional with the single element, or empty/error based on collection size
     */
    static <T> NOptional<T> ofNamedSingleton(Collection<T> collection, String name) {
        if (name == null) {
            return ofSingleton(collection, null, null);
        }
        return ofSingleton(collection,
                () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlankTrimmed(name, "value")),
                () -> NMsg.ofC("too many elements %s>1 for %s", collection == null ? 0 : collection.size(), NStringUtils.firstNonBlankTrimmed(name, "value")));
    }

    /**
     * Creates an NOptional from a collection that must contain exactly one element.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has exactly 1 element: returns NOptional wrapping that element
     * - Has more than 1 element: returns error optional with message "too many elements"
     * <p>
     * Use this when you expect exactly one result, and want to catch duplicates as errors.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.findByEmail("user@example.com");
     *   NOptional<User> user = NOptional.ofSingleton(results);
     *   // Empty if no results, User if exactly 1, Error if >1
     * </pre>
     *
     * @param collection   the collection to extract from
     * @param errorMessage optional errorMessage
     * @return NOptional with the single element, or empty/error based on collection size
     */
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
            return of(t, emptyMessage);
        }
        return ofEmpty(errorMessage);
    }

    /**
     * Creates an NOptional from the first element of a collection.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has at least 1 element: returns NOptional wrapping the first element
     * <p>
     * Use this when you only care about the first result and want to ignore the rest.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.search("active users");
     *   NOptional<User> firstActive = NOptional.ofFirst(results);
     *   // Empty if no results, first User otherwise
     * </pre>
     *
     * @param collection the collection to extract from
     * @param name       name a appear in the error message as "missing $name"
     * @return NOptional with the first element, or empty if collection is empty
     */
    static <T> NOptional<T> ofNamedFirst(Collection<T> collection, String name) {
        return ofFirst(collection,
                () -> NMsg.ofC("missing %s", NStringUtils.firstNonBlankTrimmed(name, "value"))
        );
    }

    /**
     * Creates an NOptional from the first element of a collection.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has at least 1 element: returns NOptional wrapping the first element
     * <p>
     * Use this when you only care about the first result and want to ignore the rest.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.search("active users");
     *   NOptional<User> firstActive = NOptional.ofFirst(results);
     *   // Empty if no results, first User otherwise
     * </pre>
     *
     * @param collection the collection to extract from
     * @return NOptional with the first element, or empty if collection is empty
     */
    static <T> NOptional<T> ofFirst(Collection<T> collection) {
        return ofFirst(collection, null);
    }

    /**
     * Creates an NOptional from the first element of a collection.
     * <p>
     * If the collection:
     * - Is empty: returns empty optional
     * - Has at least 1 element: returns NOptional wrapping the first element
     * <p>
     * Use this when you only care about the first result and want to ignore the rest.
     * <p>
     * Example:
     * <pre>
     *   List<User> results = db.search("active users");
     *   NOptional<User> firstActive = NOptional.ofFirst(results);
     *   // Empty if no results, first User otherwise
     * </pre>
     *
     * @param collection   the collection to extract from
     * @param emptyMessage optional message supplier
     * @return NOptional with the first element, or empty if collection is empty
     */
    static <T> NOptional<T> ofFirst(Collection<T> collection, Supplier<NMsg> emptyMessage) {
        if (emptyMessage == null) {
            emptyMessage = () -> NMsg.ofPlain("missing element");
        }
        if (collection == null || collection.isEmpty()) {
            return ofEmpty(emptyMessage);
        }
        for (T t : collection) {
            return of(t, emptyMessage);
        }
        return ofEmpty(emptyMessage);
    }

    /**
     * Throws immediately if this optional is in error state, otherwise returns unchanged.
     * Useful in chaining to detect and propagate errors early without waiting for terminal operations.
     * <p>
     * If this optional:
     * - Is empty: returns this unchanged (empty is not an error)
     * - Is error: calls {@link #get()} which throws the error
     * - Is present: returns this unchanged
     * <p>
     * Use this to fail fast in optional chains before performing expensive operations.
     * <p>
     * Example:
     * <pre>
     *   result = loadConfig()
     *       .failFast()  // Throw now if config load failed
     *       .map(cfg -> cfg.getDatabase())
     *       .map(db -> expensiveQuery(db))
     *       .orNull();
     * </pre>
     *
     * @return this optional unchanged if not in error state
     * @throws RuntimeException (NErrorOptionalException) if this optional is in error state
     */
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

    /**
     * Maps the value if this optional is present (non-empty and non-error).
     * If the optional is empty or in error state, returns an empty optional of the mapped type.
     * <p>
     * This is the most common conditional map. Use it when you want to transform
     * a value only if it exists.
     *
     * @param mapper function to apply to the value
     * @return mapped optional, or empty if this optional is not present
     */
    <V> NOptional<V> mapIfPresent(Function<T, V> mapper);

    /**
     * Maps the value if it is not blank according to {@link NBlankable#isBlank(Object)}.
     * "Blank" includes:
     * - null
     * - Empty strings or strings with only whitespace
     * - Empty arrays
     * - Empty collections or maps
     * - Objects implementing NBlankable where isBlank() returns true
     * <p>
     * Use this for user-input validation or optional string/collection handling.
     * <p>
     * Example:
     * <pre>
     *   NOptional.of("  ").mapIfNotBlank(String::trim);
     *       // Returns empty (whitespace is blank)
     *   NOptional.of("hello").mapIfNotBlank(String::trim);
     *       // Returns "hello"
     *   NOptional.of(new int[0]).mapIfNotBlank(arr -> arr.length);
     *       // Returns empty (empty array is blank)
     * </pre>
     *
     * @param mapper function to apply to the non-blank value
     * @return mapped optional, or empty if value is blank/null/empty/error
     * @see NBlankable#isBlank(Object)
     */
    <V> NOptional<V> mapIfNotBlank(Function<T, V> mapper);

    /**
     * Maps the value if it is not empty according to {@link NBlankable#isBlank(Object)}.
     * This is an alias for {@link #mapIfNotBlank(Function)}.
     * <p>
     * "Empty" includes:
     * - null
     * - Empty strings or strings with only whitespace
     * - Empty arrays
     * - Empty collections or maps
     * - Objects implementing NBlankable where isBlank() returns true
     * <p>
     * Use this when you want to explicitly convey that you're checking for "empty" state.
     * <p>
     * Example:
     * <pre>
     *   NOptional.of(Collections.emptyList()).mapIfNotEmpty(List::size);
     *       // Returns empty (list is empty)
     *   NOptional.of(List.of(1,2,3)).mapIfNotEmpty(List::size);
     *       // Returns 3
     * </pre>
     *
     * @param mapper function to apply to the non-empty value
     * @return mapped optional, or empty if value is empty/null/error
     * @see NBlankable#isBlank(Object)
     */
    <V> NOptional<V> mapIfNotEmpty(Function<T, V> mapper);

    /**
     * Maps the value if it is not null.
     * Unlike {@link #mapIfPresent(Function)}, this explicitly distinguishes between
     * a null value (which passes) and an absent/error state (which doesn't).
     * <p>
     * If this optional is:
     * - Empty or error: returns empty optional
     * - Present with null value: mapper is NOT applied, returns empty optional
     * - Present with non-null value: applies mapper
     * <p>
     * Useful when you need to treat null as a distinct state from "not present".
     * <p>
     * Example:
     * <pre>
     *   NOptional.ofNullable(null).mapIfNotNull(v -> v.toUpperCase());
     *       // Returns empty (value is null)
     *   NOptional.of("hello").mapIfNotNull(v -> v.toUpperCase());
     *       // Returns "HELLO"
     * </pre>
     *
     * @param mapper function to apply to the non-null value
     * @return mapped optional, or empty if value is null, empty, or error
     */
    <V> NOptional<V> mapIfNotNull(Function<T, V> mapper);

    /**
     * Conditionally maps this optional based on a predicate.
     * If the predicate is true, applies {@code trueExpr}; if false, applies {@code falseExpr}.
     * <p>
     * If this optional is empty or in error state, applies {@code falseExpr} with null value.
     * Both branches return their results wrapped in NOptional.
     * <p>
     * Use this for branching logic where both paths should be evaluated and result in a value.
     * <p>
     * Example:
     * <pre>
     *   NOptional<String> status = NOptional.of(user)
     *       .mapIf(u -> u.getAge() >= 18,
     *              u -> "Adult: " + u.getName(),           // True branch
     *              u -> u == null ? "Unknown" : "Minor");  // False branch
     * </pre>
     *
     * @param predicate the condition to test (null is treated as false)
     * @param trueExpr  function to apply if predicate is true
     * @param falseExpr function to apply if predicate is false or optional not present
     * @return NOptional wrapping the result of either trueExpr or falseExpr
     */
    <V> NOptional<V> mapIf(Predicate<T> predicate, Function<T, V> trueExpr, Function<T, V> falseExpr);

    /**
     * Conditionally maps this optional based on a predicate, returning the same type.
     * If the predicate is true, applies {@code trueExpr} and returns the transformed value.
     * If the predicate is false or optional is not present, returns this optional unchanged.
     * <p>
     * Use this for optional transformations where you only modify the value if a condition holds.
     * <p>
     * Example:
     * <pre>
     *   NOptional<Integer> value = NOptional.of(10)
     *       .mapIf(v -> v > 5, v -> v * 2)  // Only multiply if > 5
     *       .ifPresent(v -> log(v));         // Logs 20
     * </pre>
     *
     * @param predicate the condition to test (null is treated as false)
     * @param trueExpr  function to apply if predicate is true
     * @return NOptional wrapping the transformed value if predicate passes,
     * or this optional unchanged if predicate fails or optional not present
     */
    NOptional<T> mapIf(Predicate<T> predicate, Function<T, T> trueExpr);

    /**
     * Maps the value only if it is not the default value (set via {@link #withDefault(T)} or {@link #withDefault(Supplier)}).
     * If this optional is:
     * - Empty or error: returns unchanged
     * - Present with actual value (not default): applies mapper
     * - Present but equals the default: returns unchanged
     * <p>
     * Use this to apply transformations only to user-provided values, not defaults.
     *
     * @param mapper function to apply to non-default values
     * @return NOptional with mapper applied if value is not the default, unchanged otherwise
     */
    NOptional<T> mapIfNotDefault(Function<T, T> mapper);

    /**
     * Maps the value only if it is the default value (set via {@link #withDefault(T)} or {@link #withDefault(Supplier)}).
     * If this optional is:
     * - Empty or error: returns unchanged
     * - Present but not default: returns unchanged
     * - Present and equals the default: applies mapper
     * <p>
     * Use this to apply special handling to default values.
     *
     * @param mapper function to apply to default values
     * @return NOptional with mapper applied if value is the default, unchanged otherwise
     */
    NOptional<T> mapIfDefault(Function<T, T> mapper);

    <V> NOptional<V> mapIfNotError(Function<T, V> mapper);

    <V> NOptional<V> map(Function<T, V> mapper);

    /**
     * Casts this optional's value to a specific type if the value is an instance of that type.
     * If the value is not an instance of the target class, returns an empty optional.
     * <p>
     * Use this for safe type casting without exceptions.
     * <p>
     * Example:
     * <pre>
     *   NOptional<Object> obj = NOptional.of(getSomeObject());
     *   NOptional<String> str = obj.instanceOf(String.class);
     *       // Returns wrapped String if obj is a String, empty otherwise
     *
     *   String value = str.orNull(); // null if not a String
     * </pre>
     *
     * @param targetClass the class to check and cast to
     * @return NOptional wrapping the casted value if instance of targetClass,
     * empty optional otherwise
     */
    <V> NOptional<V> instanceOf(Class<V> targetClass);

    /**
     * Transforms this optional's value and wraps the result back into an NOptional.
     * Enables safe chaining of non-optional method calls to handle deep object graphs.
     * <p>
     * If this optional is:
     * - Not present or error: returns an empty optional
     * - Present: applies mapper and wraps result in NOptional (result can be null)
     * <p>
     * Use this for the common pattern: {@code a?.b()?.c()?.d}
     * <p>
     * Example (safe null traversal):
     * <pre>
     *   String path = NOptional.of(clazz.getProtectionDomain())
     *       .then(x -> x.getCodeSource())           // May return null
     *       .then(x -> x.getLocation())              // May return null
     *       .then(x -> x.getPath())                  // May return null
     *       .orNull(); // Gracefully returns null if any step is null
     * </pre>
     *
     * @param mapper function that takes the value and returns a non-optional result
     * @return NOptional wrapping the mapper result, or empty if this optional is not present
     */
    <V> NOptional<V> then(Function<T, V> mapper);


    /**
     * Filters this optional using a predicate that carries its own error message.
     * An NMessagedPredicate combines a {@link Predicate} and an associated error message,
     * allowing the predicate logic itself to define the failure reason.
     * <p>
     * Use this when the validation logic naturally knows why it failed.
     *
     * @param predicate a messaged predicate (predicate + error message combined)
     * @return this optional unchanged if predicate passes or optional is not present,
     * empty optional with predicate's message if predicate fails
     */
    NOptional<T> filter(NMessagedPredicate<T> predicate);

    /**
     * Filters this optional based on a predicate.
     * If the value is present and passes the predicate, returns this optional unchanged.
     * If the value is present but fails the predicate, returns an empty optional.
     * If this optional is empty or in error state, returns this optional unchanged.
     * <p>
     * Use this to add additional validation or constraints to an existing optional.
     * <p>
     * Example:
     * <pre>
     *   NOptional.of(user)
     *       .filter(u -> u.getAge() >= 18) // Passes if age >= 18
     *       .get(); // Throws NEmptyOptionalException if age check failed
     * </pre>
     *
     * @param predicate the condition to test
     * @return this optional unchanged if predicate passes or optional is not present,
     * empty optional if predicate fails
     */
    NOptional<T> filter(Predicate<T> predicate);


    /**
     * Filters this optional with a custom error message if the predicate fails.
     * Semantically equivalent to {@link #filter(Predicate)}, but allows you to provide
     * a descriptive message that will be included in the exception if {@link #get()} is called.
     * <p>
     * Example:
     * <pre>
     *   NOptional.of(user)
     *       .filter(u -> u.getAge() >= 18,
     *               () -> NMsg.ofC("User must be 18 or older, got %d", user.getAge()))
     *       .get(); // Throws with custom message if age check fails
     * </pre>
     *
     * @param predicate the condition to test
     * @param message   supplier for custom error message if predicate fails
     * @return this optional unchanged if predicate passes or optional is not present,
     * empty optional with custom message if predicate fails
     */
    NOptional<T> filter(Predicate<T> predicate, Supplier<NMsg> message);

    /**
     * Executes a side-effect operation if this optional is present (non-empty and non-error).
     * Returns this optional unchanged for chaining.
     * <p>
     * Use this to perform actions based on a present value without transforming it.
     * <p>
     * Example:
     * <pre>
     *   NOptional.of(user)
     *       .ifPresent(u -> logger.info("User: {}", u.getName()))
     *       .map(u -> u.getEmail())
     *       .ifPresent(email -> sendNotification(email));
     * </pre>
     *
     * @param action consumer function to execute on the value
     * @return this optional unchanged, for chaining
     */
    NOptional<T> ifPresent(Consumer<T> action);

    NOptional<T> ifCondition(Predicate<NOptional<T>> condition, Consumer<NOptional<T>> action);


    NOptional<T> ifNonPresent(Runnable action);

    NOptional<T> ifNull(Runnable action);

    NOptional<T> ifError(Consumer<Throwable> action);


    /**
     * Returns the value if present, otherwise returns {@code other}.
     * If this optional is in an error state, it returns {@code other}.
     *
     * @param other the value to be returned if this optional is empty or error
     * @return the value, or {@code other}
     */
    T orElse(T other);


    /**
     * Returns this optional if present, otherwise returns a new optional containing
     * the result of invoking {@code other}.
     * If the result of {@code other} is {@code null}, an empty optional is returned.
     * If this optional is in an error state, it returns a new optional containing
     * the result of invoking {@code other}.
     *
     * @param other a {@code Supplier} whose result is returned in a new NOptional if this one is empty or error
     * @return this optional if present, or a new NOptional with the result of {@code other}
     */
    NOptional<T> orElseGetOptionalOf(Supplier<T> other);

    /**
     * Returns this optional if present, otherwise returns the optional provided by the supplier.
     * This handles both **empty** and **error** states by attempting to provide a fallback optional.
     *
     * @param other a {@code Supplier} that returns an alternative {@code NOptional}
     * @return this optional if present, otherwise the optional provided by {@code other}
     */
    NOptional<T> orElseGetOptionalFrom(Supplier<NOptional<T>> other);

    /**
     * Returns the value if present, otherwise invokes {@code other} and returns the result.
     * If this optional is in an error state, it invokes {@code other} and returns the result.
     *
     * @param other a {@code Supplier} whose result is returned if this optional is empty or error
     * @return the value, or the result of {@code other}
     */
    T orElseGet(Supplier<? extends T> other);


    /**
     * Returns the contained value if present, otherwise throws an exception produced by the exception supplier.
     * If this optional is in an error state, it throws an exception produced by its internal error mechanism
     * (the default ExceptionFactory).
     *
     * @param exceptionSupplier the supplier that will return the exception to be thrown
     * @param <R>               the type of exception to be thrown
     * @return the value
     * @throws R                if this optional is empty
     * @throws RuntimeException if this optional is in an error state (throws via {@link #get()})
     */
    <R extends Throwable> T orElseThrow(Supplier<? extends R> exceptionSupplier) throws R;

    /**
     * Returns this optional if present or in an error state, otherwise returns a new optional
     * containing the provided value {@code other}.
     * This method specifically handles the **empty** state.
     *
     * @param other the value to return in a new NOptional if this one is empty
     * @return this optional if present or error, or a new NOptional with the value {@code other}
     */
    NOptional<T> onEmpty(T other);

    /**
     * Returns this optional if present or in an error state, otherwise returns the optional provided by the supplier.
     * This method specifically handles the **empty** state.
     *
     * @param other a {@code Supplier} that returns an alternative {@code NOptional}
     * @return this optional if present or error, otherwise the optional provided by {@code other}
     */
    NOptional<T> ifEmptyUse(Supplier<NOptional<T>> other);


    /**
     * Returns the contained value if present.
     * <p>
     * If this optional is:
     * - Present: returns the value.
     * - Empty: throws {@code NEmptyOptionalException}.
     * - Error: throws {@code NErrorOptionalException} (or another exception type based on the configured {@code ExceptionFactory}).
     * <p>
     * This is the "active" accessor that forces a result or an exception.
     *
     * @return the value
     * @throws RuntimeException if empty or error
     * @see #orNull()
     * @see #orDefault()
     */
    T get();

    /**
     * Returns the contained value if present, using a custom message for the exception if empty.
     * <p>
     * If this optional is:
     * - Present: returns the value.
     * - Empty: throws {@code NEmptyOptionalException} using the provided message.
     * - Error: throws {@code NErrorOptionalException} (or another exception type) using its existing error message.
     *
     * @param message custom message supplier for the exception if empty
     * @return the value
     * @throws RuntimeException if empty or error
     */
    T get(Supplier<NMsg> message);


    /**
     * Returns the {@code Throwable} that caused this optional to be in an error state.
     * Returns {@code null} if this optional is not in an error state (i.e., it is present or empty).
     *
     * @return the underlying {@code Throwable} or {@code null}
     */
    Throwable getError();

    /**
     * Returns the {@code ExceptionFactory} associated with this optional instance.
     * This factory is used to generate exceptions when {@link #get()} fails.
     *
     * @return the configured {@code ExceptionFactory}
     */
    ExceptionFactory getExceptionFactory();

    /**
     * Returns the contained value, or {@code null} if this optional is empty or an error.
     * This is the "passive" accessor — it never throws an exception.
     * <p>
     * Use this when:
     * - You want to gracefully handle missing values without exceptions
     * - You're chaining multiple nullable operations
     * <p>
     * Example:
     * <pre>
     *   String path = NOptional.of(clazz.getProtectionDomain())
     *       .then(x -> x.getCodeSource())
     *       .then(x -> x.getLocation())
     *       .then(x -> x.getPath())
     *       .orNull(); // Returns null if any step returns null
     * </pre>
     *
     * @return the value, or null if empty/error
     */
    T orNull();

    /**
     * Returns the contained value, or the configured default value if this optional is empty.
     * The default must be explicitly set using {@link #withDefault(T)} or {@link #withDefault(Supplier)}.
     * <p>
     * If this optional is:
     * - Present: returns the value
     * - Empty with default configured: returns the default
     * - Empty without default: throws NEmptyOptionalException
     * - Error: throws NErrorOptionalException
     * <p>
     * Use this when:
     * - You've explicitly configured a fallback via {@code withDefault()}
     * - You want to provide sensible defaults (e.g., 0 for numeric types, empty collections, etc.)
     * <p>
     * Example:
     * <pre>
     *   // In config class:
     *   public NOptional<NFetchStrategy> getFetchStrategy() {
     *       return NOptional.ofNamed(strategy, "fetchStrategy")
     *           .withDefault(() -> NFetchStrategy.ONLINE); // Default if not set
     *   }
     *
     *   // Usage:
     *   if (session.getFetchStrategy().orDefault() != NFetchStrategy.OFFLINE) {
     *       // Use the strategy or the default
     *   }
     * </pre>
     *
     * @return the value, the configured default, or throws if neither exists
     * @throws NEmptyOptionalException if empty with no default
     * @throws NErrorOptionalException if in error state
     * @see #withDefault(T)
     * @see #withDefault(Supplier)
     */
    T orDefault();

    /**
     * Returns the contained value, or the configured default value if this optional is empty.
     * <p>
     * If no explicit default has been configured (via {@link #withDefault(T)} or
     * {@link #withDefault(Supplier)}), this method falls back to the JVM default value
     * for the given {@code defaultType}.
     * <p>
     * The default resolution follows these steps:
     * <ul>
     *   <li>If this optional is present: returns the value</li>
     *   <li>If empty with a configured default: resolves the default recursively</li>
     *   <li>If the resolved default is {@code null} or no default is configured:
     *       returns {@link net.thevpc.nuts.reflect.NReflectUtils#getDefaultValue(Class)} for {@code defaultType}</li>
     * </ul>
     * <p>
     * If this optional is in an error state, the error is propagated.
     * <p>
     * This method is useful when a type-level default is acceptable as a final fallback
     * (for example {@code 0} for numeric types, {@code false} for booleans, or {@code null}
     * for reference types), without requiring an explicit {@code withDefault()} configuration.
     * <p>
     * Example:
     * <pre>
     *   int timeout = config.getTimeout().orDefault(int.class);
     *   boolean enabled = flags.getEnabled().orDefault(boolean.class);
     * </pre>
     *
     * @param defaultType the type whose JVM default value is used as a final fallback;
     *                    may be {@code null}, in which case {@code null} is returned
     * @return the value, the configured default, or the JVM default for {@code defaultType}
     * @throws NErrorOptionalException if in error state
     * @see #orDefault()
     * @see #withDefault(T)
     * @see #withDefault(Supplier)
     */
    T orDefault(Class<T> defaultType);

    /**
     * Wraps the result of {@link #orDefault()} back into an NOptional.
     * If this optional is present, returns an NOptional wrapping the value.
     * If this optional is empty but has a default configured, returns an NOptional wrapping the default.
     * If this optional is empty without a default **or is in an error state**, returns an empty NOptional.
     * <p>
     * Useful for chaining operations that expect NOptional return types.
     * <p>
     * Example:
     * <pre>
     * NOptional<Boolean> isBot = NOptional.ofNamed(bot, "bot")
     * .withDefault(() -> workspace.getBootOptions().getBot().orElse(false));
     *
     * // Later, use orDefaultOptional() to re-wrap for further chaining.
     * // If the original optional was an Error, this will return Empty.
     * NOptional<String> description = isBot.orDefaultOptional()
     * .map(b -> b ? "Bot mode" : "Interactive mode");
     * </pre>
     *
     * @return NOptional containing the value or default, or empty if neither exists or the optional is an Error
     * @see #orDefault()
     */
    NOptional<T> orDefaultOptional();

    /**
     * Returns an empty optional if this optional is empty, otherwise returns this optional unchanged.
     * This method is useful primarily for documentation or for consistency in chains.
     *
     * @return this optional if present or error, otherwise an empty optional
     */
    NOptional<T> ifEmptyNull();

    /**
     * Returns this optional if its value is not blank (according to {@link NBlankable#isBlank(Object)}),
     * otherwise returns a new optional containing the provided value {@code other}.
     * The blank check applies only if the optional is **present**.
     *
     * @param other the value to return in a new NOptional if this one is present but blank
     * @return this optional if present and not blank, or a new NOptional with the value {@code other}
     * @see #mapIfNotBlank(Function)
     */
    NOptional<T> onBlank(T other);

    /**
     * Returns this optional if its value is not blank, otherwise returns an empty optional
     * with a custom empty message.
     * The blank check applies only if the optional is **present**.
     *
     * @param emptyMessage supplier for the empty message if the present value is blank
     * @return this optional if present and not blank, otherwise an empty optional
     */
    NOptional<T> onBlankEmpty(Supplier<NMsg> emptyMessage);

    /**
     * Returns this optional if its value is not blank, otherwise returns an empty optional
     * with the default empty message.
     * The blank check applies only if the optional is **present**.
     *
     * @return this optional if present and not blank, otherwise an empty optional
     */
    NOptional<T> onBlankEmpty();

    /**
     * Returns an empty optional if this optional is in an error state, otherwise returns this optional unchanged.
     * This method effectively converts an error state into a non-throwing empty state.
     *
     * @return this optional if present or empty, otherwise an empty optional
     */
    NOptional<T> onErrorEmpty();

    /**
     * Returns this optional if present or empty, otherwise returns a new optional
     * containing the provided value {@code other}.
     * This method specifically handles the **error** state.
     *
     * @param other the value to return in a new NOptional if this one is in error state
     * @return this optional if present or empty, or a new NOptional with the value {@code other}
     */
    NOptional<T> onError(T other);

    /**
     * Returns this optional if its value is not blank, otherwise returns the optional provided by the supplier.
     * The blank check applies only if the optional is **present**.
     *
     * @param other a {@code Supplier} that returns an alternative {@code NOptional}
     * @return this optional if present and not blank, otherwise the optional provided by {@code other}
     */
    NOptional<T> onBlankUse(Supplier<NOptional<T>> other);

    /**
     * Returns this optional if its value is not {@code null}, otherwise returns the optional provided by the supplier.
     * The null check applies only if the optional is **present**.
     *
     * @param other a {@code Supplier} that returns an alternative {@code NOptional}
     * @return this optional if present and not null, otherwise the optional provided by {@code other}
     */
    NOptional<T> onNullUse(Supplier<NOptional<T>> other);

    /**
     * Returns an empty optional if this optional is present but holds a {@code null} value,
     * otherwise returns this optional unchanged.
     * <p>
     * If this optional is:
     * <ul>
     * <li>Present with non-null value: returns this unchanged.</li>
     * <li>Present with {@code null} value: returns a new empty optional.</li>
     * <li>Empty or Error: returns this unchanged.</li>
     * </ul>
     * <p>
     * Use this to explicitly treat a present, but null, value as a non-present (empty) state.
     *
     * @return this optional if non-null, or a new empty optional if present and null.
     */
    NOptional<T> onNullEmpty();

    /**
     * Returns this optional if it is present or empty, otherwise returns the optional provided by the supplier.
     * This method specifically handles the **error** state by providing a recovery path.
     * <p>
     * If this optional is:
     * <ul>
     * <li>Present or Empty: returns this unchanged.</li>
     * <li>Error: returns the {@code NOptional} result from {@code other}.</li>
     * </ul>
     * <p>
     * Use this when an operation might fail (return an error optional) but a reliable fallback optional is available.
     *
     * @param other a {@code Supplier} that returns an alternative {@code NOptional}
     * @return this optional if present or empty, otherwise the optional provided by {@code other}
     */
    NOptional<T> onErrorUse(Supplier<NOptional<T>> other);

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


    /**
     * Returns the current state type of this optional, which can be:
     * <ul>
     * <li>{@code PRESENT}: Value is available and not an error.</li>
     * <li>{@code EMPTY}: Value is absent (e.g., null, not found).</li>
     * <li>{@code ERROR}: An evaluation failed or an error state was explicitly set.</li>
     * </ul>
     *
     * @return the {@code NOptionalType} representing the state of this optional
     */
    NOptionalType getType();

    /**
     * Returns the message associated with this optional.
     * <ul>
     * <li>If **Empty**: Returns the empty message supplier.</li>
     * <li>If **Error**: Returns the error message supplier.</li>
     * <li>If **Present**: Returns {@code null}.</li>
     * </ul>
     * <p>
     * The supplier must be invoked to get the actual message (an {@code NMsg} instance).
     *
     * @return a {@code Supplier} for the associated {@code NMsg}, or {@code null} if present.
     */
    Supplier<NMsg> getMessage();

    /**
     * Sets a custom error message for this optional.
     * The message will be used when {@link #get()} is called and the optional is empty or error.
     * <p>
     * Use this to provide a full, specific error message for debugging or user communication.
     * <p>
     * Example:
     * <pre>
     *   NOptional.ofEmpty()
     *       .withMessage(() -> NMsg.ofC("User ID %d not found in database", userId))
     *       .get(); // Throws with custom message
     * </pre>
     *
     * @param message supplier for the full error message
     * @return this optional with custom message, for chaining
     */
    NOptional<T> withMessage(Supplier<NMsg> message);

    /**
     * Sets a custom error message for this optional.
     * The message will be used when {@link #get()} is called and the optional is empty or error.
     * <p>
     * Use this to provide a full, specific error message for debugging or user communication.
     * <p>
     * Example:
     * <pre>
     *   NOptional.ofEmpty()
     *       .withMessage(() -> NMsg.ofC("User ID %d not found in database", userId))
     *       .get(); // Throws with custom message
     * </pre>
     *
     * @param message for the full error message
     * @return this optional with custom message, for chaining
     */
    NOptional<T> withMessage(NMsg message);

    /**
     * Sets a name for this optional, used to construct an automatic error message.
     * The message will be formatted as "missing {name}" when {@link #get()} is called.
     * <p>
     * Use this as a shorthand when you want a consistent, auto-generated message format.
     * <p>
     * Example:
     * <pre>
     *   NOptional.ofEmpty()
     *       .withName("email address")
     *       .get(); // Throws with message "missing email address"
     * </pre>
     *
     * @param name NMsg representing the name
     * @return this optional with automatic message "missing {name}", for chaining
     */
    NOptional<T> withName(NMsg name);

    /**
     * Sets a name for this optional, used to construct an automatic error message.
     * The message will be formatted as "missing {name}" when {@link #get()} is called.
     * <p>
     * Use this as a shorthand when you want a consistent, auto-generated message format.
     * <p>
     * Example:
     * <pre>
     *   NOptional.ofEmpty()
     *       .withName("user email")
     *       .get(); // Throws with message "missing user email"
     * </pre>
     *
     * @param name string representing the name
     * @return this optional with automatic message "missing {name}", for chaining
     */
    NOptional<T> withName(String name);

    /**
     * Returns a new {@code NOptional} based on this one, but configured to use the provided
     * {@code ExceptionFactory} for generating exceptions when terminal methods like {@link #get()} are called.
     *
     * @param exceptionFactory the factory to use for creating exceptions
     * @return a new NOptional instance using the specified factory
     */
    NOptional<T> withExceptionFactory(ExceptionFactory exceptionFactory);

    /**
     * Converts this {@code NOptional} to a standard Java {@code Optional<T>}.
     * <p>
     * If this optional is:
     * <ul>
     * <li>Present (including present-with-null): returns a present {@code Optional} wrapping the value.</li>
     * <li>Empty: returns an empty {@code Optional}.</li>
     * <li>Error: returns an empty {@code Optional} (to remain coherent with Java's {@code Optional} which does not support an error state).</li>
     * </ul>
     * <p>
     * **No exception is thrown.**
     *
     * @return a standard Java {@code Optional}
     */
    Optional<T> asOptional();

    /**
     * Returns a sequential {@code NStream} with the contained value if this optional is present.
     * <p>
     * If this optional is:
     * <ul>
     * <li>Present: returns a stream containing only the value.</li>
     * <li>Empty: returns an empty stream.</li>
     * <li>Error: returns an empty stream (to avoid throwing an exception).</li>
     * </ul>
     *
     * @return a sequential {@code NStream}
     */
    NStream<T> stream();

    /**
     * Factory interface used to customize the creation of exceptions thrown by
     * {@code NOptional} when terminal methods (like {@link NOptional#get()})
     * encounter a failure condition (Empty or Error), as well as exceptions
     * thrown by related utility classes (like assertion helpers).
     */
    interface ExceptionFactory {

        /**
         * Creates a {@code RuntimeException} to be thrown when a value is expected,
         * but the {@link NOptional} is in the **Empty** state.
         *
         * @param message the descriptive message for the empty state
         * @return the runtime exception to throw (e.g., NEmptyOptionalException)
         */
        RuntimeException createOptionalEmptyException(NMsg message);

        /**
         * Creates a {@code RuntimeException} to be thrown when a terminal method
         * is called on an optional that is in the **Error** state.
         *
         * @param message the descriptive error message
         * @param e       the underlying throwable that caused the error state, if available (may be null)
         * @return the runtime exception to throw (e.g., NErrorOptionalException)
         */
        RuntimeException createOptionalErrorException(NMsg message, Throwable e);

        /**
         * Creates a {@code RuntimeException} to be thrown for general **assertion failures**
         * within the Nuts framework (e.g., by the {@code NAssert} utility).
         *
         * @param message the assertion failure message
         * @param e       the underlying throwable, if available (may be null)
         * @return the runtime exception to throw
         */
        RuntimeException createAssertException(NMsg message, Throwable e);

        /**
         * Creates a {@code RuntimeException} specifically for **command-line related errors**,
         * typically encountered during option parsing, validation, or command execution.
         *
         * @param message the command-line error message
         * @param e       the underlying throwable, if available (may be null)
         * @return the runtime exception to throw
         */
        RuntimeException createCmdLineException(NMsg message, Throwable e);
    }
}
