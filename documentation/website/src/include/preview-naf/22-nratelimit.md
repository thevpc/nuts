---
title: Running Limited Actions with claimAndRun
subTitle:  |
  Instead of consuming tokens immediately, <code>claimAndRun</code> will wait until the limiter can provide a slot before executing your code. This is useful when you want the action to eventually run rather than fail.
  You can also define multiple independent limits (per minute, per day, etc.) in a single limiter, and <code>claimAndRun</code> will respect all limits before executing.
contentType: java
---

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
