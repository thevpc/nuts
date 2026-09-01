---
title: NRetryCall
---

# NRetryCall

`NRetryCall` lets you execute tasks with retry capabilities, including configurable retry periods, recover actions, and handlers. This is useful for tasks that may fail intermittently, such as network calls or database operations.

---

## Basic Example

```java
// Create a simple retry call
NRetryCall<String> retryCall = NRetryCall.of(() -> {
            NOut.println("Trying...");
            if (new Random().nextBoolean()) {
                throw new RuntimeException("Failed");
            }
            return "Success";
        });

// Set maximum retries and retry period
retryCall.setMaxRetries(5)
         .setRetryPeriod(Duration.ofSeconds(1));

// Execute blocking
String result = retryCall.call();
NOut.println("Result: " + result);
```

### Asynchronous Execution

```java
retryCall.callAsync();

// Or retrieve a future
Future<NRetryCall.Result<String>> future = retryCall.callFuture();
NRetryCall.Result<String> result = future.get();
if (result.isValid()) {
    NOut.println("Succeeded: " + result.result());
} else {
    NOut.println("Failed after retries");
}
```

### Linear Backoff
Linear backoff increases the wait linearly with each attempt.

```java
retryCall.setMultipliedRetryPeriod(Duration.ofSeconds(1), 2.0);
// Waits: 0s, 2s, 4s, 6s, 8s...
```

### Exponential Backoff
Exponential backoff increases the wait exponentially with each attempt.

```java
retryCall.setExponentialRetryPeriod(Duration.ofSeconds(1), 2.0);
// Waits: 1s, 2s, 4s, 8s, 16s...
```

### Custom Recover Action
You can provide a recovery callable if all retries fail.

```java
retryCall.setRecover(() -> {
    NOut.println("Recovering...");
    return "Recovered Result";
});

String result = retryCall.callOrElse(() -> "Default Result");
```

### Custom Handler
Handlers are notified of each result, success or failure.

```java
retryCall.setHandler(result -> {
    if (result.isValid()) {
        NOut.println("Success: " + result.result());
    } else {
        NOut.println("Failure for retry call id: " + result.id());
    }
});

```

### Factory Usage with ID

```java
NRetryCallFactory factory = NConcurrent.of().retryCallFactory();

NRetryCall<String> retryCallWithId = factory.of("myCallId", () -> {
    return "Task Result";
});

retryCallWithId.setMaxRetries(3)
               .setRetryPeriod(Duration.ofSeconds(2))
        .call();

```
