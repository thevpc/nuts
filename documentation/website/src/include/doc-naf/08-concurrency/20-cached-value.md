---
title: Cached Value
---


`NCachedValue` helps you cache expensive computations or
resources.
It evaluates a `Supplier` once, stores the result, and
reuses it until
the cache expires or is invalidated.
You can configure expiry policies to automatically refresh values.


```java
// Example 1: Cache with expiry
NCachedValue&lt;Double> cachedRandom = NCachedValue.of(Math::random)
        .setExpiry(Duration.ofSeconds(5));

// First call computes and caches the value
        NOut.println("First value = " + cachedRandom.get());

// Subsequent calls reuse the cached value (within 5 seconds)
        NOut.println("Cached value = " + cachedRandom.get());

// Invalidate to force recomputation
        cachedRandom.invalidate();
NOut.println("New value after invalidate = " + cachedRandom.get());

```

Sometimes a computation may fail (for example, a remote call).
`NCachedValue` can automatically retry, retain the last good value
on failure, and recover gracefully.
This makes it ideal for unstable resources or intermittent network services.

```java

// Example 2: Cache with retries and fallback
AtomicInteger counter = new AtomicInteger();

NCachedValue&lt;Integer> cached = NCachedValue.of(() -> {
    int attempt = counter.incrementAndGet();
    if (attempt % 2 == 0) {
        throw new RuntimeException("Simulated failure");
    }
    return attempt;
})
.setRetry(3, Duration.ofMillis(100))   // retry up to 3 times
.retainLastOnFailure(true);            // keep last value if failure occurs

// First call computes and caches
NOut.println("Value = " + cached.get());

// Next call may fail internally but still returns last good value
NOut.println("Resilient value = " + cached.get());

```
