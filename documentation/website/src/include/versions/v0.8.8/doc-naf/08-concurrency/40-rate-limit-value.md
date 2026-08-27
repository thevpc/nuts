---
title: Rate Limit Value
---


`NRateLimitValue` lets you control how often actions can be executed.
For example, you can allow only **10 actions every 2 minutes**, using a strategy like **sliding window**.
This helps protect APIs, services, or expensive operations from overuse.

### Example 1: Basic sliding window rate limit

```java
NRateLimitedValue limiter = NRateLimitedValue.ofBuilder("example")
        .withLimit("calls", 10).per(Duration.ofMinutes(2))
        .withStrategy(NRateLimitDefaultStrategy.SLIDING_WINDOW)
        .build();

for (int i = 0; i < 15; i++) {
    NRateLimitValueResult res = limiter.take();
    if (res.success()) {
        NOut.println("Action " + i + " allowed at " + Instant.now());
    } else {
        NOut.println("Action " + i + " rejected. Retry after "
                     + res.getRetryAfter().orElse(Duration.ZERO));
    }
}
```

### Example 2: Using `claimAndRun`

```java
limiter.claimAndRun(() -> {
    NOut.println("Doing a limited action...");
});

// Multiple independent limits (per minute, per day, etc.)
NRateLimitValue limiter2 = NRateLimitValue.ofBuilder("api-calls")
        .withLimit("per-minute", 60).per(Duration.ofMinutes(1))
        .withLimit("per-day", 1000).per(Duration.ofDays(1))
        .build();

limiter2.claimAndRun(() -> {
    NOut.println("API call allowed");
});
```

### Example 3: Using `claimAndCall`

```java
limiter.claimAndCall(() -> {
    return expensiveOperation();
});
```

### Features

* Immediate vs Deferred Execution: `take()` vs `claim()`
* Fluent builder for limits
* Multiple independent limits per value
* Optional persistence via store
* Strategy-based control (sliding window, custom)
* Thread-safe and suitable for concurrent environments

### Notes

* `claimAndRun` blocks until a permit is available and then executes the runnable.
* `take()` or `takeAndCall` try to acquire permits immediately.
* `NRateLimitValueResult` provides fluent callbacks for success/failure handling.
* Multiple strategies can be defined via the factory.
* Limits can be defined in duration units (`per(Duration)`).
