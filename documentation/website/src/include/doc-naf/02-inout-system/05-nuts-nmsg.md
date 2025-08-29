---
title: NMsg for Messages and text formatting
---


The `NMsg` class is a powerful utility for building **structured**, **formatted**, and **styled** messages in **Nuts**. Messages constructed using `NMsg` can be safely rendered in various output formats (e.g., plain text, NTF, JSON, etc.) and support placeholders, syntax coloring, and semantic styling.

`NMsg` is fully integrated with `NOut` and `NErr` for displaying rich and meaningful CLI output.

## Basic Message Construction

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


## Styling Output
In addition to content, NMsg supports visual styling, including colors and font modes.
### Style by Color Index (Predefined Color Schemes)
Use built-in style presets based on Nuts theme indexes:

```java
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledPrimary1("world")));
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledSecondary5("world")));
```
These correspond to configurable UI themes, ensuring consistency across color schemes.

### Style by Foreground Color

You can apply arbitrary Java Color objects:

```java
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledForegroundColor("world", Color.RED)));
```

This sets the foreground color (text color) to red. Works with any java.awt.Color as far as the current terminal supports it.

### Style by Mode (Blink, Bold, etc.)

Use text styles for visual emphasis:

```java
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledBlink("world", Color.RED)));
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledBold("world")));
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledStriked("world")));
```

You can combine color and style for higher impact.

### Style by Semantic Token Type

`NMsg` also supports semantic styling, commonly used in syntax highlighting:

```java
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledComments("world")));
NOut.println(NMsg.ofC("Hello %s", NMsg.ofStyledWarn("world")));
```

These apply styles based on meaning:

- `ofStyledComments`: renders like source code comments.
- `ofStyledWarn`: renders with a warning color/style.

Additional token types include:

- `ofStyledString(...)`
- `ofStyledKeyword(...)`
- `ofStyledBoolean(...)`
- `ofStyledError(...)`

# Summary
The `NMsg` class allows you to:

- Format messages using multiple conventions (C-style, Java-style, named variables),
- Style message fragments with semantic types or custom colors,
- Seamlessly integrate with `NOut`, `NErr`, `NPrintStream`,... for rich CLI or structured outputs.

Whether you're building user-facing tools or internal diagnostics, `NMsg` ensures your messages are clear, consistent, and visually expressive.


