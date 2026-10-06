---
id: NOptional
title: NOptional
sidebar_label: NOptional
---

# NOptional

`NOptional<T>` is a tri-state container that evolves Java’s `Optional` for real-world enterprise and library code.  
By moving beyond the simple **Present/Absent** model, `NOptional` enables safer, more expressive, and composable null-safe code, closely mirroring the capabilities of modern languages like TypeScript and Kotlin.

It explicitly models three distinct outcomes of any computation or data lookup:

| State       | Meaning                                       | Typical Origin                              | Java `Optional` Equivalent             |
|-------------|-----------------------------------------------|---------------------------------------------|----------------------------------------|
| **PRESENT** | A value is available (may be `null`)          | Successful evaluation                       | `Optional.of(...)` / `ofNullable(...)` |
| **EMPTY**   | The value is logically absent                 | Not found, filtered out, blank input        | `Optional.empty()`                     |
| **ERROR**   | A technical or logical failure occurred       | Exception during evaluation, explicit error | *(not supported)*                      |

This distinction is fundamental: a missing configuration key (`EMPTY`) is not the same as a malformed XML file that could not be parsed (`ERROR`). Collapsing both into a single “absent” state loses diagnostic power and forces awkward external `try/catch` blocks.

`NOptional` is designed for fluent, composable, null-safe code while remaining fully interoperable with the JDK (`asOptional()`, `jstream()`, etc.).


## 1. Core Design Principles

- 1. **Tri-state semantics** – Present / Empty / Error are first-class and never collapsed.
- 2. **Named values & rich diagnostics** – Every empty or error state can carry a descriptive `NMsg`. Calling `get()` produces meaningful exceptions automatically.
- 3. **Configurable exception factories** – Applications and libraries can plug in their own exception types via `ExceptionFactory`.
- 4. **Deep, short-circuiting navigation** – `then(...)` is the direct equivalent of the safe-navigation operator (`?.`) found in Kotlin, TypeScript, C#, etc.
- 5. **Blank-aware operations** – Integration with `NBlankable` makes empty strings, whitespace-only strings, empty collections/arrays, and custom blank objects first-class citizens.
- 6. **Explicit recovery points** – Dedicated methods for recovering from empty vs. error (`ifEmptyUse`, `onErrorUse`, `ifErrorThrow`, …).
- 7. **Zero-surprise terminal operations** – `get()`, `orNull()`, `orElse(...)`, `orDefault()`, etc. have precise, documented contracts.

## 2. Creating NOptionals

### Basic Factories

```java
// Explicitly allows null (PRESENT holding null)
NOptional.ofNullable(value); // value may be null
NOptional.ofNullable(value, () -> NMsg.ofC("custom empty message"));

// Present only if non-null; otherwise EMPTY
// Treats null as EMPTY
NOptional.of(value);
NOptional.of(value, () -> NMsg.ofC("missing %s", "user"));

// Explicit empty
        NOptional.ofEmpty();
NOptional.ofEmpty(() -> NMsg.ofC("user not found"));
        NOptional.ofNamedEmpty("user");          // → "missing user"
NOptional.ofNamedEmpty(NMsg.ofC("user"));

// Explicit error
        NOptional.ofError(() -> NMsg.ofC("failed to load config"));
        NOptional.ofError(throwable);
NOptional.ofNamedError("config", throwable);

// From Java Optional
NOptional.ofOptional(javaOptional);
NOptional.ofNamedOptional(javaOptional, "user");
```

### Important Distinction: Present-with-null

Unlike Java’s `Optional`, `NOptional` can hold an explicit `null` value in the **PRESENT** state:

| Call                        | State     | `isPresent()` | `isNull()` | Notes                                      |
|-----------------------------|-----------|---------------|------------|--------------------------------------------|
| `NOptional.ofNullable(null)`| PRESENT   | `true`        | `true`     | Explicit null is preserved                 |
| `NOptional.of(null)`        | EMPTY     | `false`       | `false`    | Null is treated as absence                 |
| `Optional.ofNullable(null)` | empty     | `false`       | —          | Java collapses null into empty             |

This allows callers to distinguish between:
- “I received a null” (PRESENT + null)
- “The value is missing / not found” (EMPTY)
- “An error occurred while retrieving the value” (ERROR)


### Collection Helpers

```java
// Exactly one element expected
NOptional.ofSingleton(collection);                    // EMPTY if 0, PRESENT if 1, ERROR if >1
NOptional.ofNamedSingleton(collection, "user");

// First element (ignore the rest)
NOptional.ofFirst(collection);
NOptional.ofNamedFirst(collection, "user");
```

### Lazy / Deferred Evaluation
```java
NOptional.ofSupplier(() -> expensiveLookup());
NOptional.ofCallable(() -> service.findUser(id));
```

### Named Values and Custom Messages

By using `ofNamed("user")`, your resulting exception (when calling `get()`) is automatically generated with a descriptive message like "Missing required value: user." This eliminates the need for manual exception message creation and relies on the configurable **`ExceptionFactory`** for consistent error types.


## 3. Terminal Operations – Retrieving the Value

| Method                  | Present        | Empty                                              | Error                                             |
|:------------------------|:---------------|:---------------------------------------------------|:--------------------------------------------------|
| `get()`                 | returns value  | throws NEmptyOptionalException                     | throws NErrorOptionalException                    |
| `get(Supplier<NMsg>)`   | returns value  | throws NEmptyOptionalException with custom message | throws NErrorOptionalException with cutom message |
| `orNull()`              | returns value  | returns null                                       | returns null                                      |
| `orElse(T)`             | returns value  | returns fallback                                   | returns fallback                                  |
| `orElseGet(Supplier)`   | returns value  | returns evaluated supplier                         | returns evaluated supplier                        |
| `orDefault()`           | returns value  | returns configured default                         | returns configured default                        |
| `orDefault(Class)`      | returns value  | returns configured or JVM default                  | returns configured or JVM default                 |
| `orElseThrow(Supplier)` | returns value  | throws supplied exception                          | throws supplied exception                         |


### Boolean helpers (useful for flags):

```java
boolean flag = optional.orFalse();   // EMPTY/ERROR → false
boolean flag = optional.orTrue();    // EMPTY/ERROR → true
```

## 4. State Inspection

```java
boolean isPresent() / isNotPresent()
boolean isEmpty()
boolean isNull()          // PRESENT holding null
boolean isError()
NOptionalType type()      // PRESENT | EMPTY | ERROR
Supplier<NMsg> message()
Throwable getError()
ExceptionFactory getExceptionFactory()
```

### Rule of thumb
- Use `get()` when the value must exist (assertion).
- Use `orNull()` / `orElse(...)` when absence is acceptable.
- Prefer `orDefault()` when a sensible default has been declared with `withDefault(...)`.


## 5. Transformations (Map/Filter)


### Mapping Family

| Method                              | Behaviour                                         |
|:------------------------------------|:--------------------------------------------------|
| `map(Function)`                     | Classic map; EMPTY/ERROR stay EMPTY/ERROR         |
| `mapIfPresent(Function)`            | Maps only when PRESENT                            |
| `mapIfNotNull(Function)`            | Maps only when PRESENT and value ≠ null           |
| `mapIfNotBlank(Function)`           | Maps only when PRESENT and not blank (NBlankable) |
| `mapIfNotEmpty(Function)`           | Alias of mapIfNotBlank                            |
| `mapIf(Predicate, Function)`        | Conditional map (same type)                       |
| `mapIf(Predicate, trueFn, falseFn)` | Full if/else map                                  |
| `mapIfNotDefault / mapIfDefault`    | Respects withDefault(...)                         |
| `mapIfNotError(Function)`           | Maps only when not ERROR                          |
| `flatMap(Function)`                 | Flat-map to another NOptional                     |


```java
// NOptional: uses the integrated NBlankable logic
String cleanToken = NOptional.of(readProperty("auth.token"))
    .mapIfNotBlank(String::trim) // Filters null, "", and " "
    .orNull();
```



### Fluent Navigation & Mapping

#### Safe Deep Traversal – `then(...)`

`NOptional` introduces clean mechanisms for asserting a value's presence and providing context-rich exceptions, dramatically improving debugging and developer experience.

`then` short-circuits on `EMPTY` or `ERROR` and never throws.

```java
// Classic Java
Number roadNumber = (app != null
        && app.person != null
        && app.person.address != null
        && app.person.address.road != null)
        ? app.person.address.road.number
        : null;

// NOptional
Number roadNumber = NOptional.of(app)
        .then(a -> a.person)
        .then(p -> p.address)
        .then(a -> a.road)
        .then(r -> r.number)
        .orNull();
```

#### Combining Assertion and Chaining

`NOptional` chains can freely combine passive operations (`then(...)`) with assertive ones (`get()`) to enforce mandatory states within a larger flow.

```java 
// Equivalent to: var roadNumber = app?.person?.address!.road?.number ?? 0;
Number roadNumber = NOptional.of(app)
                .then(v -> v.person)
                .then(v -> v.address)
                .get() // ASSERT: Throws if address is null/empty
                .then(v -> v.road)
                .then(v -> v.number)
                .orElse(0); // Coalesce: Fallback to 0 if road or number is null/empty
```

| Concept                   | Java Equivalent (Verbose)                             | `NOptional` (Expressive)                                   | Equivalent TS               |
|:--------------------------|:------------------------------------------------------|:-----------------------------------------------------------|:----------------------------|
| **Mandatory Value Check** | ```if (user == null) throw new NEx("missing user")``` | **```NOptional.ofNamed(user, "user").get()```**            | ```user!```                 |
| **Null-Safe Mapping**     | ```if (user != null) user.toUpperCase()```            | ```NOptional.of(user).map(String::toUpperCase).orNull()``` | ```user?.toUpperCase()```   |
| **Nullish Coalescing**    | ```user != null ? user : "default"```                 | **```NOptional.of(user).orElse("default")```**             | ```user ?? "default"```     |
| **Error Recovery**        | ```try { ... } catch (E) { fallback }```              | **```load().ifErrorUse(() -> defaultOptional).get()```**   | *(No direct TS equivalent)* |


#### Optional Chaining for Deep Traversal (`then(...)`)

`NOptional` provides the `then(...)` method for fluent and safe traversal of deep object graphs, acting as a direct analog to the safe-navigation operator (`?.`) in modern languages. It short-circuits the chain if any part returns `null` or is `EMPTY`.

| Code Style         | Example                                                                                                                           |
|:-------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| **Java (Verbose)** | ```Number roadNumber = (app != null && app.person != null && app.person.road != null) ? app.person.address.road.number : null;``` |
| **NOptional**      | ```Number roadNumber = NOptional.of(app).then(v -> v.person).then(v -> v.road).then(v -> v.number).orNull();```                   |




#### Combining Assertion and Navigation

```java
// Equivalent to: app?.person?.address!.road?.number ?? 0
Number roadNumber = NOptional.of(app)
        .then(a -> a.person)
        .then(p -> p.address)
        .get()                    // ASSERT: address must be present
        .then(a -> a.road)
        .then(r -> r.number)
        .orElse(0);
```


### Filtering

```java
optional
    .filter(u -> u.getAge() >= 18)
    .filter(u -> u.getAge() >= 18, () -> NMsg.ofC("must be 18+, got %d", u.getAge()))
    .filter(NMessagedPredicate...);   // predicate that carries its own message
```

## 6. Error & Empty Recovery

```java
// Fail fast on ERROR (EMPTY is left alone)
optional.ifErrorThrow();

// Recover from ERROR
optional.onErrorUse(() -> fallbackOptional);
optional.onError(defaultValue);
optional.onErrorEmpty();          // ERROR → EMPTY

// Recover from EMPTY
optional.ifEmptyUse(() -> fallbackOptional);
optional.onEmpty(defaultValue);

// Blank-aware recovery
optional.onBlank(defaultValue);
optional.onBlankUse(() -> fallback);
optional.onBlankEmpty();
optional.onNullEmpty();
optional.onNullUse(() -> fallback);
```

## 7. Side-effect & Conditional Execution

```java
optional
    .ifPresent(value -> log.info("got {}", value))
    .ifNonPresent(() -> log.warn("missing"))
    .ifNull(() -> log.debug("explicit null"))
    .ifError(ex -> log.error("failed", ex))
    .ifCondition(opt -> opt.isPresent() && someFlag, opt -> ...);
```

## 8. Defaults, Messages & Exception Factories

```java

// Attach a default that is used by orDefault() / orDefaultOptional()
public NOptional<NFetchStrategy> getFetchStrategy() {
    return NOptional.ofNamed(strategy, "fetchStrategy")
            .withDefault(() -> NFetchStrategy.ONLINE);
}

// Usage
NFetchStrategy fs = session.getFetchStrategy()
        .mapIfNotBlank(s -> NFetchStrategy.parse(s).orNull())
        .orDefault();

// Custom messages
optional.withName("user email");                 // → "missing user email"
optional.withMessage(() -> NMsg.ofC("..."));

// Per-instance exception factory
optional.withExceptionFactory(myFactory);

// Global factory (affects all NOptionals)
NOptional.setDefaultExceptionFactory(myFactory);

```

The default factory produces:
- NEmptyOptionalException / NDetachedEmptyOptionalException
- NErrorOptionalException / NDetachedErrorOptionalException
depending on whether an NWorkspace context is available.


## 9. Interoperability

```java
Optional<T> jdk = noptional.asOptional();     // ERROR becomes empty
NStream<T>  stream = noptional.stream();
Stream<T>   jstream = noptional.jstream();
```

## 10. Why NOptional Instead of java.util.Optional?

| Limitation of `Optional`                     | How `NOptional` solves it                     |
|:---------------------------------------------|:----------------------------------------------|
| No ERROR state                               | First-class `ERROR` + recovery methods        |
| Verbose deep chaining (`flatMap`)            | `then(...)` – concise safe navigation         |
| No named values / poor diagnostics           | `ofNamed`, `withName`, rich `NMsg` exceptions |
| No blank handling                            | `mapIfNotBlank`, `onBlank`… via `NBlankable`  |
| Exception type hard-coded                    | Pluggable `NOptionalExceptionFactory`                  |
| Defaults must be supplied at every call site | `withDefault` + `orDefault()`                 |
| No collection helpers                        | `ofSingleton`, `ofFirst`, …                   |


`NOptional` remains fully compatible with functional style and can always be converted back to a JDK `Optional` when required.

## 11. Related Types

- NOptionalType – the three-state enum (PRESENT, EMPTY, ERROR)
- NBlankable – blank detection contract used by many methods
- NMsg – structured, localizable messages
- NOptionalExceptionFactory – custom exception creation
- NStream – the streaming counterpart

> NOptional is part of the Nuts framework. It is designed to be used both inside Nuts applications and as a standalone utility in any Java project that needs robust optional handling.


