# Customizing ANSI Themes

## Overview

Nuts provides a powerful theming system for ANSI/NTF (Nuts Text Format) output. Themes determine the actual colors used for semantic tokens such as `primary1`, `error`, `warn`, etc. You can switch between built‑in themes or create your own.

## Switching Themes at Runtime

```java
import net.thevpc.nuts.text.NTextTheme;

// Load a theme by its name (e.g., "dark", "light", "horizon")
NTextTheme theme = NTextTheme.of("dark");
// Apply the theme globally
Nuts.textTheme().setTheme(theme);
```

You can also set the theme via a configuration property or environment variable:

- **Configuration property**: `nuts.text.theme = dark`
- **Environment variable**: `NUTS_TEXT_THEME=dark`

## Defining Your Own Theme

Create a `.ntf-theme` file in any location accessible to your application. The file format is a simple `key=value` list where keys are semantic token names and values are color specifications.

```properties
# example.ntf-theme
primary1 = #1e90ff
secondary5 = #ffdead
error = #ff5555
warn = #ffb86c
info = #8be9fd
```

Place your custom theme file on the classpath (e.g., `src/main/resources/META-INF/ntf-themes/`) or load it explicitly:

```java
NTextTheme custom = NTextTheme.load("classpath:/my-theme.ntf-theme");
Nuts.textTheme().setTheme(custom);
```

## Built‑in Theme Examples

The repository ships a few example themes you can use as a starting point:

- **example.ntf-theme** – a balanced default theme.
- **horizon.ntf-theme** – a dark blue horizon style.
- **min.ntf-theme** – a minimalistic light theme.

You can find these files under `src/resources/theme-examples/`.

## Applying Themes to Specific Output

If you need to render a message with a non‑global theme, use the `NText` API directly:

```java
NText themed = NText.of("Hello World").withTheme(custom);
NOut.println(themed);
```

## Further Reading

- `NTextTheme` class: `net.thevpc.nuts.text.NTextTheme`
- Default runtime implementation: `net.thevpc.nuts.runtime.standalone.text.DefaultNTextRPI`
- Theme resource location: `META-INF/ntf-themes/`

For more details, refer to the [Styling Messages](../03-msg/02-nmsg-styling.md) section.
