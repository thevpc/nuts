---
title: Caching with NCachedValue
subTitle:  |
  <code>NCachedValue</code> helps you cache expensive computations or
  resources.
  It evaluates a <code>Supplier</code> once, stores the result, and
  reuses it until
  the cache expires or is invalidated.
  You can configure expiry policies to automatically refresh values.
contentType: java
---

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
