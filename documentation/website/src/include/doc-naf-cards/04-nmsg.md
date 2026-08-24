---
title: NMsg in a nutshell
subTitle:  |
            <code>NMsg</code> allows you to create structured messages with
            dynamic <strong>placeholders</strong>, automatically rendering
            common types such as <strong>booleans</strong>,
            <strong>numbers</strong>, <strong>paths</strong>,
            <strong>dates</strong>, and <strong>enums</strong> in colors for
            improved readability. You can also apply custom styles to
            placeholders or entire messages, and even nest messages within
            messages to build complex, rich, and expressive content. This makes
            <code>NMsg</code> a versatile tool for clear, context-aware, and
            visually informative output in any application.
contentType: java
---

// Simple example with automatic coloring
NMsg.ofC("User %s set flag %s on path %s",
    "Alice", true, NPath.of("/tmp/data.txt")
);

// Custom styling example
NMsg.ofC("Task %s completed with status %s",
    "Upload",
    NText.ofStyled("OK", NTextStyle.primary1())
);

// Nested messages example
NMsg.ofV("User $user completed ${task}",
    NMaps.of(
        "user", "Alice",
        "task", NMsg.ofV("task %s in %s", "Upload",
                 NText.ofStyled("123ms", NTextStyle.secondary1()))
    )
);
