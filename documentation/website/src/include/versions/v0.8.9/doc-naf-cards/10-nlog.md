---
title: Unified Logging with NLog
subTitle:  |
  <code>NLog</code> combines structured messages (<code>NMsg</code>),
  styled output with colors and <code>NTF</code> formatting, and
  seamless integration with <strong>JUL</strong> or
  <strong>SLF4J</strong>. Messages carry an <strong>Intent</strong>,
  similar to logging markers, which allows you to classify, filter, or
  style logs at runtime. Whether you’re building CLI tools, scripts,
  or full applications, <code>NLog</code> makes logs expressive, rich,
  and easy to understand.
contentType: java
---

NLog.of("myapp").info(
NMsg.ofV("Hello %s, task %s completed with status %s",
"user","Alice","status",
NText.ofStyled("OK", NTextStyle.primary1()), exception
)
.withIntent(NMsgIntent.CONFIG)   // Classifies this log, can be used for filtering or styling
.withThrowable(exception)        // Attaches the exception
.withDurationMs(123)             // Optional: report task duration
);
