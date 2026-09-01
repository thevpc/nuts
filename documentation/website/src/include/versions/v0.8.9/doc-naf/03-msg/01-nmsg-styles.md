---
title: Placeholder Formats
---

NMsg supports multiple placeholder formats for dynamic message generation:
- C-style (`ofC`) – like `printf` in C (`%s`, `%d`, etc.)
- Java / SLF4J style (`ofJ`) – `{}` or `{0}`, `{1}`
- Variable substitution (`ofV`) – named placeholders using `$name` or `${name}`

Examples
```java
// C-style formatting
NMsg.ofC("Hello %s, you have %d new notifications", "Alice", 5);

// Java formatting
NMsg.ofJ("Downloading {0} from {1}", "report.pdf", "server-01");

// SLF4J-style formatting
NMsg.ofJ("Downloading {} from {}", "report.pdf", "server-01");

// Variable substitution from map
NMsg.ofV("User $user on ${app}", Map.of("user", "Alice", "app", "NAF"));

// Variable substitution from function
NMsg.ofV("Threshold=$th, Date=$date", name -> switch (name) {
    case "th"   -> 0.85;
    case "date" -> LocalDate.now();
    default     -> null;
});
```
Notes:
- Avoid mixing styles in a single message.
- `${}` syntax is safer for complex strings (e.g., `$val123text` vs `${val}123text`).

### C-style Formatting (`ofC`)

Use `ofC` to create messages using standard `String.format()`-style syntax:

```java
NOut.println(NMsg.ofC("Hello %s", "world"));
```
Placeholders like `%s`, `%d`, etc., behave as expected. Useful for simple messages with positional arguments.

### Java MessageFormat (ofJ)

Use `ofJ` for Java-style formatting with `{0}`, `{1}` placeholders:

```java
NOut.println(NMsg.ofJ("Hello {0}", "world"));
NOut.println(NMsg.ofJ("Hello {}", "world"));      // SLF4J-style
```

Both formats are supported, and will be filled using the provided arguments in order (but should not be mixed).
- `{}` placeholders are matched sequentially, like in SLF4J.
- `{0}`, `{1}`, etc. allow for specific argument reordering or reuse.

## Variable-based Formatting (ofV)
Use `ofV` to format messages using named variables:

```java
NOut.println(NMsg.ofV("Hello $v", NMaps.of("v", "world")));
NOut.println(NMsg.ofV("Hello ${v}", NMaps.of("v", "world")));
```
Both `$v` and `${v}` syntaxes are supported.

Variables are replaced by name using the $ prefix. This is useful for dynamically named arguments or template-based rendering,
particularly when formatting messages from dynamic key-value maps (e.g., for templates or localization).

- $v is simple and concise.
- ${v} is safer when followed by alphanumeric characters (e.g., `$val123text` vs `${val}123text`).

Missing variables are left as-is or replaced with a placeholder, depending on context or configuration.


