---
title: Rate Limit Value
---


<code>NRateLimitValue</code> lets you control how often actions can
be executed.
For example, you can allow only <em>10 actions every 2 minutes</em>,
using a strategy like <em>sliding window</em>.
This makes it easy to protect APIs, services, or expensive
operations from overuse.


```java
// Example 1: Basic sliding window rate limit
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

Instead of consuming tokens immediately, <code>claimAndRun</code> will wait until the limiter can provide a slot before executing your code. This is useful when you want the action to eventually run rather than fail.
You can also define multiple independent limits (per minute, per day, etc.) in a single limiter, and <code>claimAndRun</code> will respect all limits before executing.

```java
/// Example 2: Using claimAndRun
limiter.claimAndRun(() -> {
    NOut.println("Doing a limited action...");
});

// Adding multiple limits at once
NRateLimitedValue limiter2 = NRateLimitedValue.ofBuilder("api-calls")
        .withLimit("per-minute", 60).per(Duration.ofMinutes(1))
        .withLimit("per-day", 1000).per(Duration.ofDays(1))
        .build();

limiter2.claimAndRun(() -> {
    NOut.println("API call allowed");
});

```
