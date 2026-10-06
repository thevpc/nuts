---
title: Nested Messages
---

You can nest messages to create complex, styled outputs:

```java
// Custom styling example
NMsg.ofC("Task %s completed with status %s",
    "Upload",
    NText.ofStyled("OK", NTextStyle.primary1())
);
```

Nested messages combine formatting, styling, and placeholders dynamically.

```java
// Nested messages example
NMsg.ofV("User $user completed ${task}",
    NMaps.of(
        "user", "Alice",
        "task", NMsg.ofV("task %s in %s", "Upload",
        NText.ofStyled("123ms", NTextStyle.secondary1()))
    )
);
```
