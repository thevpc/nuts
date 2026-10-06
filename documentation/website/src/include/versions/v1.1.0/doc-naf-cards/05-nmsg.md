---
title: Placeholder Formats
subTitle:  |
  <code>NMsg</code> supports multiple placeholder formats to create
  flexible and dynamic messages. You can use C-style placeholders
  (<code>%s</code>, <code>%d</code>) for traditional printf-style
  formatting, Java-style placeholders (<code>{}</code>) similar to
  MessageFormat or <strong>SLF4J</strong>, and variable substitution (<code>$name</code>)
  for named placeholders. The variable substitution format can take
  parameters, maps, or functions, making it easy to build messages
  dynamically and adaptively for your application.
contentType: java
---

NMsg.ofC("Hello %s, you have %d new notifications", "Alice", 5);
NMsg.ofJ("Downloading {} from {}", "report.pdf", "server-01");
NMsg.ofV("User $user on ${app}", Map.of("user", "Alice", "app", "NAF"));
NMsg.ofV("Threshold=$th, Date=$date", name -> switch (name) {
    case "th"   -> 0.85;
    case "date" -> LocalDate.now();
    default     -> null;
});
