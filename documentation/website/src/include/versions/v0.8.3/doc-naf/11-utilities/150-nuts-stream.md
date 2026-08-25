---
id: NStream
title: NStream
sidebar_label: NStream
---
`NStream<T>` is A lazy, describable, sequential pipeline that can be executed when a terminal operation is called — exactly like `Stream`, plus the ability to ask “what is this pipeline?” at any moment before execution.

It wraps and extends Java’s `Stream` API to deliver **describable, inspectable, and framework-integrated** pipelines while remaining fully compatible with standard stream operations.

NStream is **not a replacement** for `java.util.stream.Stream`. It is a thin, zero-overhead wrapper that adds:

- Structured pipeline descriptions (`describe()`)
- Seamless integration with NAF types (`NElement`, `NMsg`, `NOptional`, etc.)
- Convenient factories for arrays, iterables, iterators, optionals, and empty streams
- Additional terminal helpers (`findSingleton()`, `toSortedSet()`, typed primitive arrays, etc.)
- Safe consumption semantics and explicit close handling

It implements `Iterable<T>`, `NRedescribable<NStream<T>>`, and `AutoCloseable`.

---

## 1. Examples of usage

```java
// From values
NStream<Integer> s1 = NStream.of(1, 2, 3, 4, 5);

// From a Java Stream
Stream<String> javaStream = Stream.of("a","b","c");
NStream<String> s2 = NStream.ofStream(javaStream);

```

```java
// From Iterable or Iterator
List<Double> numbers = List.of(0.1, 0.2, 0.3);
NStream<Double> s3 = NStream.of(numbers);
```

```java
// From any object
NStream<Double> s3 = NStream.ofSingleton(1.0);
```

```java
// From Optional
NOptional<Double> number = NOptional.of(1);
NStream<Double> s3 = NStream.ofOptional(number);
```

NStream works transparently over all these, giving you a single, uniform API.

NStream supports familiar operations:
```java
NStream<Integer> s = NStream.of(1,2,3,4,5)
                             .filter(x -> x % 2 == 0)
                             .map(x -> x * 10);
```
You can use any combination of map, filter, flatMap, sorted, etc., just like a standard Java Stream.

```java
NStream<Integer> s = NStream.ofArray(1,2,3,4,5)
    .filter(NPredicate.of(x -> x % 2 == 0)
                      .withDesc(() -> NElement.ofString("even numbers")))
    .map(NFunction.of(x -> x * 10)
                  .withDesc(NElement.ofObject("mul", NElement.ofNumber(10))));

NElement description = s.describe();
NOut.println(description);
```


## 2. Creating an NStream

### Quick Reference – Factory Methods

```java
NStream.ofArray(T...)
NStream.ofIntArray(int...)
NStream.ofLongArray(long...)
NStream.ofDoubleArray(double...)
NStream.ofBooleanArray(boolean...)
NStream.ofByteArray(byte...)
NStream.ofCharArray(char...)
NStream.ofShortArray(short...)
NStream.ofFloatArray(float...)
NStream.ofStream(Stream<T>)
NStream.ofIterable(Iterable<T>)
NStream.ofIterator(Iterator<T>)
NStream.ofOptional(NOptional<T> | Optional<T>)
NStream.ofSingleton(T)
NStream.ofEmpty()
```

### From values / arrays

```java
// Varargs
NStream<Integer> s1 = NStream.ofArray(1, 2, 3, 4, 5);

// Primitive arrays (with automatic description)
NStream<Integer> ints   = NStream.ofIntArray(1, 2, 3);
NStream<Long>    longs  = NStream.ofLongArray(10L, 20L);
NStream<Double>  doubles= NStream.ofDoubleArray(1.1, 2.2);
NStream<Boolean> bools  = NStream.ofBooleanArray(true, false);
// similarly: ofByteArray, ofCharArray, ofShortArray, ofFloatArray
```

### From Java Stream

```java
Stream<String> javaStream = Stream.of("a", "b", "c");
NStream<String> s2 = NStream.ofStream(javaStream);
```

### From Iterable / Iterator

```java
List<Double> numbers = List.of(0.1, 0.2, 0.3);
NStream<Double> s3 = NStream.ofIterable(numbers);

Iterator<String> it = ...;
NStream<String> s4 = NStream.ofIterator(it);
```

### From Optional

```java
NOptional<Double> nOpt = NOptional.of(1.0);
NStream<Double> s5 = NStream.ofOptional(nOpt);

Optional<String> jOpt = Optional.of("hello");
NStream<String> s6 = NStream.ofOptional(jOpt);
```

### Singleton & empty

```java
NStream<Double> singleton = NStream.ofSingleton(1.0);
NStream<String> empty     = NStream.ofEmpty();
```

All factory methods produce a uniform NStream surface, so downstream code never needs to know the original source type.

## 3. Core Stream Operations

NStream supports the familiar intermediate and terminal operations of Java Streams:

```java
NStream<Integer> result = NStream.ofArray(1, 2, 3, 4, 5)
    .filter(x -> x % 2 == 0)
    .map(x -> x * 10)
    .sorted()
    .distinct();
```

### Intermediate operations

| Method                                                                              | Description                                    |
|:------------------------------------------------------------------------------------|:-----------------------------------------------|
| `map(Function)`                                                                     | Transform each element                         |
| `mapUnsafe(UnsafeFunction)`                                                         | Transform that may throw checked exceptions    |
| `mapUnsafe(UnsafeFunction, Function<Exception,R>)`                                  | Transform with error recovery                  |
| `filter(Predicate)`                                                                 | Keep matching elements                         |
| `flatMap(...)` / `flatMapStream` / `flatMapList` / `flatMapArray` / `flatMapIter`   | Flatten nested structures                      |
| `flatMapToInt` / `flatMapToLong` / `flatMapToDouble`                                | Flatten to primitive streams                   |
| `instanceOf(Class<V>)`                                                              | Keep only instances of the given type (cast)   |
| `nonNull`                                                                           | Drop null elements                             |
| `nonBlank`                                                                          | Drop null / blank strings and any `NBlankable` |
| `distinct` / `distinct`                                                             | Remove duplicates                              |
| `sorted` / `sorted(Comparator)`                                                     | Sort                                           |
| `skip(long)` / `limit(long)`                                                        | Windowing                                      |
| `concat(...)` / `coalesce(long)`                                                    | Append another stream / iterator               |


### Terminal operations
| Method                                                               | Description                                       |
|:---------------------------------------------------------------------|:--------------------------------------------------|
| MethodDescriptiontoList() / toSet() / toSortedSet() / toOrderedSet() | Materialize collections                           |
| toArray(IntFunction)                                                 | Typed array                                       |
| toIntArray() / toLongArray() / …                                     | Primitive arrays                                  |
| toMap / toOrderedMap / toSortedMap                                   | Build maps                                        |
| groupBy / groupedBy                                                  | Grouping                                          |
| findFirst() / findAny() / findLast()                                 | Find elements                                     |
| findSingleton()                                                      | Exactly one element (throws otherwise)            |
| count()                                                              | Element count                                     |
| min / max                                                            | ExtremumanyMatch / allMatch / noneMatchPredicates |
| collect(...)                                                         | Custom collectors                                 |
| jstream()                                                            | Convert back to a Java Stream                     |
| iterator()                                                           | Obtain an NIterator                               |

> Important: Most terminal operations consume the stream. Calling them twice yields undefined behaviour (empty result or exception). Prefer collecting once into a list/set if you need multiple passes.


## 4. Describable Pipelines

The primary value of NStream is the ability to describe the pipeline for logging, debugging, reporting, and NAF search/command introspection.
Use the describable functional interfaces NFunction, NPredicate, NComparator etc.:

```java
NStream<Integer> pipeline = NStream.ofArray(1, 2, 3, 4, 5)
    .filter(NPredicate.of(x -> x % 2 == 0)
                     .withDesc(() -> NElement.ofString("even numbers")))
    .map(NFunction.of(x -> x * 10)
                 .withDesc(NElement.ofObject("mul", NElement.ofNumber(10))));

NElement description = pipeline.describe();
NOut.println(description);
```

Typical structured output:

```json
{
  "source": [1, 2, 3, 4, 5],
  "operations": [
    { "filter": "even numbers" },
    { "map": { "mul": 10 } }
  ]
}
```
- Descriptions are optional. You can freely mix plain Java lambdas with describable ones.
- `withDescription(Supplier<NElement>)` can also be applied at the stream level.
- Because `NStream` implements `NRedescribable`, you can re-describe an existing pipeline without rebuilding it.

## 5. Resource Management

NStream implements AutoCloseable. Always close streams that hold resources (files, network, etc.):

```java
try (NStream<String> lines = NStream.ofStream(Files.lines(path))) {
    lines.filter(...).forEach(...);
}
```
You can also register close handlers:

```java
stream.onClose(() -> resource.release());
```

## 6. Why Use NStream?
- Visibility :  describe() yields a structured NElement view of the entire pipeline – ideal for debugging, logging and NAF search commands.
- Uniform API :  One surface for arrays, iterables, iterators, optionals and Java streams.
- NAF Integration :  Native support for NElement, NMsg, NOptional, NComparator, etc.
- Extra helpers :  findSingleton(), typed primitive arrays, nonBlank(), coalesce(), ordered/sorted maps & sets.
- Optional overhead :  If you never call describe(), behaviour and performance are essentially identical to a plain Java Stream.

> NStream shines in NAF search pipelines, command implementations, logging, reporting, and any place where you want inspectable data-processing steps.



## Notes & Best Practices

1. Prefer `NStream.ofArray(...)` / `ofIntArray(...)` when you want automatic source description.
2. Use `findSingleton()` when the pipeline is expected to produce exactly one element; it fails fast otherwise.
3. Convert back to a Java Stream only when you need APIs that accept `Stream` (e.g. third-party libraries): `stream.jstream()`.
4. Descriptions are evaluated lazily; expensive description suppliers are safe.
5. Because the stream is consumable, collect early if you need multiple terminal operations.

For the full method list and Javadoc, see the `NStream` interface.

