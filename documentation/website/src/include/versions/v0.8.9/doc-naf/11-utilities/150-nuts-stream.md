---
id: NStream
title: NStream
sidebar_label: NStream
---
Java’s Stream API is powerful, but NAF introduces NStream to provide additional features useful in application development and debugging, particularly when you want describable, inspectable, and framework-integrated streams.

NStream is not meant to replace Stream, but rather to wrap and extend it, providing:

- Describable operations for logging, debugging, and reporting
- Integration with NAF features like NElement, NMsg, and structured outputs
- Helper methods that simplify working with iterables, iterators, or streams

## Creating NStream

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

## Stream Operations

NStream supports familiar operations:
```java
NStream<Integer> s = NStream.of(1,2,3,4,5)
                             .filter(x -> x % 2 == 0)
                             .map(x -> x * 10);
```
You can use any combination of map, filter, flatMap, sorted, etc., just like a standard Java Stream.

## Describable Pipelines

One of NStream’s main benefits is that you can describe the operations for inspection or reporting. This is done using describable functional interfaces like NFunction and NPredicate:

```java
NStream<Integer> s = NStream.ofArray(1,2,3,4,5)
    .filter(NPredicate.of(x -> x % 2 == 0)
                      .withDesc(() -> NElement.ofString("even numbers")))
    .map(NFunction.of(x -> x * 10)
                  .withDesc(NElement.ofObject("mul", NElement.ofNumber(10))));

NElement description = s.describe();
NOut.println(description);
```

Example output (structured NElement):
```json
{
  "source": [1,2,3,4,5],
  "operations": [
    {"filter": "even numbers"},
    {"map": {"mul": 10}}
  ]
}
```

NOTE : Descriptions are optional. You can still use plain Stream functions if you don’t need introspection.

## Why Use NStream?


- Visibility – describe() gives a structured view of the pipeline, useful for debugging and reporting.
- Consistency – Unified API over Streams, Iterables, and Iterators.
- NAF Integration – Works seamlessly with NElement, NMsg, and other NAF utilities.
- Optional – You can ignore the descriptive features and use it just like a standard Stream.
- NStream is particularly helpful in NAF search commands, logging pipelines, or anywhere you want structured insight into the data processing steps.
