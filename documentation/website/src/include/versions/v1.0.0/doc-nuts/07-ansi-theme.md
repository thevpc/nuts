# Customizing ANSI Themes

## Overview

Nuts provides a powerful theming system for ANSI and NTF (Nuts Text Format) formatted output. Themes map semantic text styles (such as `PRIMARY`, `KEYWORD`, `ERROR`, `WARN`, `INFO`, `PATH`, etc.) to specific terminal colors, supporting 16-color ANSI, 256-color palettes, and 24-bit RGB true-colors.

The `--theme` option and the `NTextTheme` API support specifying theme parameters by **theme name** (for built-in or cached themes) or by **file path / URL** (for custom `.ntf-theme` files).

## Built-in Themes & Default Names

Nuts includes several built-in themes available on the classpath (`META-INF/ntf-themes/`). You can reference them directly by name:

- **`default`** – OS-dependent default theme (`grass` on Windows, standard theme on Unix/Linux).
- **`ansi`** – Basic 16-color ANSI palette theme.
- **`grass`** – Green/nature-toned palette, optimized for Windows terminals.
- **`horizon`** – Dark blue horizon theme, default on Unix/Linux.
- **`whiteboard`** – Light background theme using 24-bit true colors.

When no theme name or path is provided (or when set to `default`), Nuts automatically selects the appropriate default theme for the running operating system environment.

## Setting Themes at Boot or Runtime

### Via Command Line Option (`--theme`)

The `--theme` CLI option accepts either a built-in theme name or a file path/URL to a custom theme file.

#### 1. By Theme Name
Pass one of the default theme names (`default`, `ansi`, `grass`, `horizon`, `whiteboard`):

```sh
nuts --theme=horizon
```

#### 2. By File Path or URL
Pass a file path (relative or absolute) or URL to a `.ntf-theme` file:

```sh
nuts --theme=/path/to/my-theme.ntf-theme
```

### Via Java API

The `NTextTheme.of(String nameOrPath)` factory method resolves themes seamlessly:
- **Simple Name**: Loads built-in theme resources from `classpath:/META-INF/ntf-themes/<name>.ntf-theme` or user themes from `~/.config/nuts/.../themes/<name>`. Themes loaded by name are cached.
- **File Path or URL**: Loads the theme from the specified filesystem path or URL via `NPath`.
- **Null or Blank**: Loads the default theme configured for the workspace/OS environment.

#### Example Usage

```java
import net.thevpc.nuts.text.NTextTheme;
import net.thevpc.nuts.io.NPath;

// Load a theme by built-in name
NTextTheme themeByName = NTextTheme.of("horizon").orNull();
if (themeByName != null) {
    NTextTheme.set(themeByName);
}

// Load a theme by file path
NTextTheme themeByPath = NTextTheme.of("/path/to/my-theme.ntf-theme").orNull();
if (themeByPath != null) {
    NTextTheme.set(themeByPath);
}

// Using NPath explicitly
NTextTheme themeFromNPath = NTextTheme.of(NPath.of("/path/to/my-theme.ntf-theme")).orNull();
```

## Defining Your Own Theme

Themes are defined in `.ntf-theme` property files. A theme file consists of key-value pairs defining:
1. Optional theme metadata (e.g. `theme-name=my-theme`).
2. Optional custom color/palette variables (e.g., `MY_BLUE=4`, `DARK_RED=#670000`).
3. Mapping rules for semantic token styles.

### Syntax & Format

```properties
# example.ntf-theme
theme-name=my-theme

# Palette variables (ANSI numbers 0-255 or 24-bit hex colors)
DARK_BLUE=4
BRIGHT_BLUE=12
DARK_SKY=6
DARK_RED=#670000

# Primary and Secondary base palette styles with variant index
PRIMARY(0)=foregroundColor(DARK_BLUE)
PRIMARY(1)=foregroundColor(BRIGHT_BLUE)
PRIMARY(*)=PRIMARY(*%16)

SECONDARY(0)=backgroundColor(DARK_BLUE)
SECONDARY(*)=SECONDARY(*%16)

# Title style combining primary and underline
TITLE(*)=primary(*),underlined()

# Syntax & Token Styles
KEYWORD(0)=foregroundColor(BRIGHT_BLUE)
KEYWORD(1)=foregroundColor(DARK_SKY)
KEYWORD(*)=KEYWORD(*%4)

OPTION(0)=foregroundColor(DARK_SKY)
OPTION(*)=KEYWORD(*%4)

# Semantic UI & Status Styles
ERROR(*)=foregroundColor(DARK_RED)
SUCCESS(*)=foregroundColor(2)
WARN(*)=foregroundColor(3)
INFO(*)=foregroundColor(DARK_SKY)
CONFIG(*)=foregroundColor(5)
DATE(*)=foregroundColor(6)
NUMBER(*)=foregroundColor(6)
BOOLEAN(*)=foregroundColor(6)
STRING(*)=foregroundColor(8)
SEPARATOR(*)=foregroundColor(208)
OPERATOR(*)=foregroundColor(208)
INPUT(*)=foregroundColor(11)
FAIL(*)=foregroundColor(DARK_RED)
DANGER(*)=foregroundColor(DARK_RED)
VAR(*)=foregroundColor(190)
PALE(*)=foregroundColor(250)
COMMENTS(*)=foregroundColor(250)
VERSION(*)=foregroundColor(220)
PATH(*)=foregroundColor(114)
```

### Supported Token Styles

Supported semantic style tokens include:
- Base: `PRIMARY`, `SECONDARY`, `TITLE`
- Syntax: `KEYWORD`, `ENTITY`, `ACTION`, `ANNOTATION`, `VAR`, `OPERATOR`, `SEPARATOR`, `COMMENTS`
- Literals: `STRING`, `INPUT`, `PATH`, `VERSION`, `NUMBER`, `DATE`, `BOOLEAN`, `OPTION`, `PLACEHOLDER`
- UI Status: `INFO`, `CONFIG`, `SUCCESS`, `WARN`, `ERROR`, `DANGER`, `FAIL`, `PALE`

### Supported Styling Functions

- Modifiers: `plain`, `underlined`, `bold`, `blink`, `striked`, `reversed`, `italic`
- Colors: `foregroundColor(val)` / `foreground(val)`, `backgroundColor(val)` / `background(val)`, `foregroundTrueColor(val)`, `backgroundTrueColor(val)` or direct `#RRGGBB` hex values.

### Custom Theme Locations

Place custom theme files in:
1. The application classpath under `META-INF/ntf-themes/<name>.ntf-theme`.
2. The Nuts user configuration directory under `~/.config/nuts/.../themes/<name>`.
3. Any accessible filesystem location loaded by path or URL using `--theme=/path/to/theme.ntf-theme` or `NTextTheme.of(NPath.of(...))`.

## Further Reading

- `NTextTheme` interface: `net.thevpc.nuts.text.NTextTheme`
- Default theme implementation: `net.thevpc.nuts.runtime.standalone.text.theme.NTextPropertiesTheme`
- Built-in theme resources: `META-INF/ntf-themes/`

For more details, refer to the [Styling Messages](../03-msg/02-nmsg-styling.md) section.
