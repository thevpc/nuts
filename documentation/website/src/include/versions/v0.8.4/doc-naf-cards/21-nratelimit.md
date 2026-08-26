---
title: Rate Limiting with NRateLimitValue
subTitle:  |
  <code>NRateLimitValue</code> lets you control how often actions can
  be executed.
  For example, you can allow only <em>10 actions every 2 minutes</em>,
  using a strategy like <em>sliding window</em>.
  This makes it easy to protect APIs, services, or expensive
  operations from overuse.
contentType: java
---

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
