---
title: Task Set
---

`NTaskSet` lets you manage multiple asynchronous tasks as a single logical set.
You can submit tasks as `Future`, `CompletableFuture`, `Callable`, `Runnable`, `Supplier`, or `NCallable`, and then wait for completion, get results, or handle errors in a consistent way.

This is useful for executing batches of work concurrently, coordinating results, or stopping remaining tasks once one finishes.

### Creating a Task Set

```java
NTaskSet tasks = NTaskSet.of()
.call(() -> "Hello from callable")
.run(() -> NOut.println("Running a simple task"))
.supply(() -> computeValue());
```

### Waiting for All Tasks

Use `join()` to block until all tasks complete.
Any exceptions are captured and can be retrieved via `errors()`.

```java
tasks.join();  // waits for all tasks
List<?> results = tasks.results();  // collect all results (null for failed tasks)
List<Throwable> errors = tasks.errors();  // collect exceptions if any
```

### Getting the First Completed Result

`first()` returns the result of the first task that completes.
Other tasks continue to run unless you pass `true` to cancel them:

```java
String firstResult = tasks.first();  // peek first result, do not cancel others
String firstAndCancel = tasks.first(true);  // returns first and cancels remaining tasks
```

`firstOnly()` is equivalent to `first(true)`:

```java
String winner = tasks.firstOnly();
```

### Requiring All Tasks to Succeed

If you want to ensure all tasks completed successfully, use `requireAll()`.
The first exception encountered will be thrown.

```java
tasks.requireAll(); // throws CompletionException if any task failed
List<?> allResults = tasks.results();
```

### Adding Tasks Dynamically

You can add tasks at any time:

```java
tasks.add(CompletableFuture.supplyAsync(() -> "Dynamic task"));
tasks.call(() -> "Callable with executor", executorService);
tasks.run(() -> cleanup());
```

### Cancelling All Tasks

You can cancel all running or pending tasks:

```java
tasks.cancelAll(true); // true = may interrupt running tasks
```

### Checking Task State

```java
if (tasks.isDone()) {
NOut.println("All tasks completed");
}
if (tasks.hasError()) {
NOut.println("Some tasks failed");
}
```

`NTaskSet` provides a simple, fluent API for orchestrating concurrent work with flexible error handling and cancellation strategies.
