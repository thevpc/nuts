---
title: Resilient Caching with Retries
subTitle:  |
  Sometimes a computation may fail (for example, a remote call).
  <code>NCachedValue</code> can automatically retry, retain the last good value
  on failure, and recover gracefully.
  This makes it ideal for unstable resources or intermittent network services.
contentType: java
---

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
