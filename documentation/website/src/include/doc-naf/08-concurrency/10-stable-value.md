---
title: Stable Value
---


<code>NStableValue</code> stores a value that is computed lazily — the supplier is not
invoked until the first call to <code>get()</code>. Once evaluated, the value remains stable
and is reused for all subsequent accesses. This is perfect for expensive computations,
constants, or resources that should only be initialized once.
contentType: java


```java
// Example 1: Lazy initialization
NStableValue&lt;Double> stableRandom = NStableValue.of(Math::random);

// Value is computed on first access
NOut.println("First value = " + stableRandom.get());

// Subsequent accesses return the same value
NOut.println("Same value = " + stableRandom.get());

// Check evaluation status
NOut.println("Evaluated? " + stableRandom.isEvaluated());
NOut.println("Valid? " + stableRandom.isValid());
```
