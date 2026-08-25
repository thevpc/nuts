---
title: Placeholder Formats
---

NMsg supports multiple placeholder formats for dynamic message generation:
- C-style (`ofC`) – like `printf` in C (`%s`, `%d`, etc.)
- Java / SLF4J style (`ofJ`) – `{}` or `{0}`, `{1}`
- Variable substitution (`ofV`) – named placeholders using `$name` or `${name}`
- Moustache substitution (`ofM`) – named placeholders using `{{name}}`
- Sql substitution (`ofS`) – positional and named placeholders using `?` or `:name`
- Custom substitution (`ofCustom`) – user defined format by implementing `NMsgCustomFormatter`

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


// SQL-style positional formatting
// Beware you are responsible for escaping the strings
NMsg.ofS("SELECT * FROM users WHERE status = ? AND age >= ?", "\"ACTIVE\"", 21);

// SQL substitution from function
// Beware you are responsible for escaping the strings
NMsg.ofV("Select a from Table where a.column=:value", name -> switch (name) {
        case "value"   -> 0.85;
default     -> null;
        });
   
        
// Variable substitution from function (with mustache)
NMsg.ofM("Threshold={{th}}, Date={{date}}", name -> switch (name) {
    case "th"   -> 0.85;
    case "date" -> LocalDate.now();
    default     -> null;
});

// Custom registered formatter
NMsg.ofCustom("upper", "hello world");
```
Notes:
- Avoid mixing styles in a single message.
- `${}` syntax is safer for complex strings (e.g., `$val123text` vs `${val}123text`).
- `{ { } }` syntax is safer when '$' has specific meanings in your context.

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



## Variable-based Moustache Formatting (ofM)
Use `ofM` to format messages using named variables with Mustache-style placeholders:

```java
NOut.println(NMsg.ofV("Hello {{v}}", NMaps.of("v", "world")));
```

Variables are replaced by name using Mustache-style. This is useful for dynamically named arguments or template-based rendering,
particularly when formatting messages from dynamic key-value maps (e.g., for templates or localization).
This isolates variables completely from string payloads that might natively use the $ character.
Missing variables are left as-is or replaced with a placeholder, depending on context or configuration.


## SQL-style Formatting (ofS)
Use ofS for SQL-style queries using ? positional placeholders:

```java
// SQL-style positional formatting
// Beware: you are responsible for escaping the strings
NMsg.ofS("SELECT * FROM users WHERE status = ? AND age >= ?", "\"ACTIVE\"", 21);

// SQL variable substitution from function
// Beware: you are responsible for escaping the strings
NMsg.ofV("Select a from Table where a.column=:value", name -> switch (name) {
        case "value"   -> 0.85;
default     -> null;
        });

        
```

Because NMsg builds an abstract AST (NText) rather than interacting directly with a JDBC driver, parameter values are inserted literally into the node tree. Callers must handle any required SQL escaping or quoting manually.


## Custom Formatting (ofCustom)

You can extend NMsg by registering a custom implementation of NMsgCustomFormatter.

Custom formatters can be registered dynamically at runtime via NExtensions or auto-discovered using standard Java SPI 
(`META-INF/services/net.thevpc.nuts.spi.NComponent`) paired with `@NScore` for priority ordering (when needed).

```java
// Register custom formatter dynamically
NExtensions.of().registerInstance(NMsgCustomFormatter.class, new NMsgCustomFormatter() {
    @Override
    public String id() {
        return "upper";
    }

    @Override
    public NText format(NMsg msg) {
        String m = (String) msg.message();
        return NText.ofPlain(m.toUpperCase());
    }

    @Override
    public List<String> extractParams(String message) {
        return Collections.emptyList();
    }
});

// Execute custom formatter by ID
NMsg msg = NMsg.ofCustom("upper", "hello");
```