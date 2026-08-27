---
id: NOptional
title: NOptional
sidebar_label: NOptional
---

# NOptional

`NOptional` is a powerful, tri-state alternative to Java’s standard `Optional`. It evolves the concept of an optional container to handle the full complexity of data retrieval in enterprise applications, **semantically differentiating between missing data and technical failures**.

By moving beyond the simple **Present/Absent** model, `NOptional` enables safer, more expressive, and composable null-safe code, closely mirroring the capabilities of modern languages like TypeScript and Kotlin.

| State | Meaning | Standard `Optional` Equiv. | `NOptional` Method |
| :--- | :--- | :--- | :--- |
| **PRESENT** | A value exists (which may be `null`). | `Optional.of(...)` | `ofNullable(...)` |
| **EMPTY** | A value is logically missing. | `Optional.empty()` | `ofEmpty(...)` |
| **ERROR** | A technical failure occurred during evaluation. | *(Not Supported)* | `ofError(...)` |

***

## 💡 The Core Architectural Advantage: Tri-State Modeling

Java's `Optional` forces you to handle technical failures (like I/O or parsing errors) with external `try/catch` blocks or by collapsing them into a simple `empty` state, losing context.

`NOptional` solves this by explicitly modeling the **ERROR** state. This is crucial when chaining operations: you can differentiate between a configuration value that was **not found (`EMPTY`)** and one that **failed to load due to malformed XML (`ERROR`)**.

Methods like `failFast()` (to throw immediately on error) and `ifErrorUse()` (to define a fallback optional) allow for precise control over error propagation and recovery within the fluent chain.

***

## Fluent Null and Error Assertion

`NOptional` introduces clean mechanisms for asserting a value's presence and providing context-rich exceptions, dramatically improving debugging and developer experience.

| Concept                   | Java Equivalent (Verbose)                             | `NOptional` (Expressive)                                   | Equivalent TS               |
|:--------------------------|:------------------------------------------------------|:-----------------------------------------------------------|:----------------------------|
| **Mandatory Value Check** | ```if (user == null) throw new NEx("missing user")``` | **```NOptional.ofNamed(user, "user").get()```**            | ```user!```                 |
| **Null-Safe Mapping**     | ```if (user != null) user.toUpperCase()```            | ```NOptional.of(user).map(String::toUpperCase).orNull()``` | ```user?.toUpperCase()```   |
| **Nullish Coalescing**    | ```user != null ? user : "default"```                 | **```NOptional.of(user).orElse("default")```**             | ```user ?? "default"```     |
| **Error Recovery**        | ```try { ... } catch (E) { fallback }```              | **```load().ifErrorUse(() -> defaultOptional).get()```**   | *(No direct TS equivalent)* |

### Named Values and Custom Messages

By using `ofNamed("user")`, your resulting exception (when calling `get()`) is automatically generated with a descriptive message like "Missing required value: user." This eliminates the need for manual exception message creation and relies on the configurable **`ExceptionFactory`** for consistent error types.

***

## Optional Chaining for Deep Traversal (`then(...)`)

`NOptional` provides the `then(...)` method for fluent and safe traversal of deep object graphs, acting as a direct analog to the safe-navigation operator (`?.`) in modern languages. It short-circuits the chain if any part returns `null` or is `EMPTY`.

| Code Style         | Example                                                                                                                           |
|:-------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| **Java (Verbose)** | ```Number roadNumber = (app != null && app.person != null && app.person.road != null) ? app.person.address.road.number : null;``` |
| **NOptional**      | ```Number roadNumber = NOptional.of(app).then(v -> v.person).then(v -> v.road).then(v -> v.number).orNull();```                   |


### Combining Assertion and Chaining

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

### Advanced Built-in Filters and Mappers
`NOptional` provides specialized methods that handle common, tedious data validation cases, relying on the core utility `NBlankable` to define "absence."


| Feature              | Method                   | Description                                                                                                                 | Example Use Case                                                           |
|:---------------------|:-------------------------|:----------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------|
| **Blank Filtering**  | ```mapIfNotBlank(...)``` | Filters out strings that are empty, contain only whitespace, or collections/arrays that are empty (using NBlankable rules). | Reading non-mandatory config properties.                                   |
| **Error Fallback**   | ```ifErrorUse(...)```    | Recovers the entire optional chain if it enters the ERROR state, providing a fallback NOptional.                            | Switching from a primary API call (error) to a cache lookup (fallback).    |
| **Conditional Map ** | ```mapIf(...)```         | Executes a map operation only if a specific predicate is met, returning the optional unchanged otherwise.                   | Applying a transformation (e.g., trimming) only if a certain flag is set.  |


#### Example: Handling Blank Values

```java
// NOptional: uses the integrated NBlankable logic
String cleanToken = NOptional.of(readProperty("auth.token"))
    .mapIfNotBlank(String::trim) // Filters null, "", and " "
    .orNull();
```


## When to Use get(), orNull(), and orElse()

These three terminal operations define the contract for retrieving the value:

* `get()` – returns the value or throws an exception if the optional is Empty or Error (Assertive/Mandatory).
* `orNull()` – returns the value or returns null if the optional is Empty or Error (Passive/Safe).
* `orElse(value)` – returns the value or returns a defined fallback if the optional is Empty or Error (Coalescing/Fallback).


## Why not just use Optional

While Java’s built-in Optional is functional, it lacks the necessary features for large-scale, robust application development:

- ❌ No Tri-State Model: Errors must be handled and propagated outside the optional chain.
- ❌ Verbose Chaining: Optional chaining is heavy (flatMap) for deep object traversals.
- ❌ Poor Diagnostics: No support for named values or custom exception factories.
- ❌ No Utility Integration: No built-in support for filtering "blank" data.

`NOptional` solves all these architectural limitations while maintaining full compatibility with functional idioms and providing asOptional() for conversion when interoperability is required.
