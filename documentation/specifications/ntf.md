# 📄 NTF Specification (v1.0)

**NTF** (Nuts Text Format) is a Markdown-like text format designed for rich text styling, coloring, and semantic formatting in terminal environments. It is the standard format used by the **Nuts** platform for console output, error messages, and help manuals.

---

## 1. Core Concepts

### 1.1 Rationale
NTF aims to provide a human-readable and easy-to-write format that can be rendered into various outputs (ANSI terminals, HTML, plain text) while preserving semantic meaning and visual richness.

### 1.2 Special Characters
The following characters have special meaning in NTF:
- `#`: Used for styles and titles.
- `` ` ``: Used for verbatim text and code blocks.
- `\`: Escape character.
- `:`: Separator inside `#` styles.
- `{`, `}`: Used for composed styles.
- `\u001E`: A 'nop' (no-operation) character used as a separator when needed (not displayed).

### 1.3 Escaping
Any special character can be escaped using a backslash `\`.
```ntf
\# This is not a title
\\ This is a literal backslash
```
For escaping large blocks of text, use verbatim blocks.

---

## 2. Styles and Colors

### 2.1 Primary Styles (Titles)
Styles are defined by the number of `#` characters. When used at the **start of a line** (preceded by a newline and optional spaces) followed by a parenthesis `)`, they act as hierarchical titles.

```ntf
#) Title Level 1
  ##) Title Level 2 (Indentation is allowed)
###) Title Level 3
```

### 2.2 Inline Styles
Inline styles use the `##style: text##` or `##text##` syntax.

| Syntax | Description |
| :--- | :--- |
| `##Text##` | Primary Style 1 (Title 1) |
| `###Text###` | Primary Style 2 (Title 2) |
| `####Text####` | Primary Style 3 (Title 3) |
| `##:bold: Text##` | **Bold** text (or `##+: Text##`) |
| `##:italic: Text##` | *Italic* text (or `##/: Text##`) |
| `##:underlined: Text##` | <u>Underlined</u> text (or `##_: Text##`) |
| `##:striked: Text##` | ~~Striked~~ text (or `##-: Text##`) |
| `##:reversed: Text##` | Reversed colors (or `##!: Text##`) |
| `##:blink: Text##` | Blinking text (or `##%: Text##`) |

### 2.3 Colors
NTF supports several color depths:

- **Theme Colors**:
    - Foreground: `##:p<0-15>: Text##` or `##:<0-15>: Text##` (e.g., `##:p1: Text##` or `##:1: Text##`).
    - Background: `##:s<0-15>: Text##`.
    - Named: `##:primary<0-15>: Text##` or `##:secondary<0-15>: Text##`.
- **4-bit Colors**: `##:f<0-15>: Text##` (Foreground) or `##:b<0-15>: Text##` (Background).
- **8-bit Colors**: `##:f<0-255>: Text##` or `##:b<0-255>: Text##`.
- **24-bit Colors**: `##:fx<RRGGBB>: Text##` or `##:bx<RRGGBB>: Text##`.

### 2.4 Composed Styles
Multiple styles can be combined using `##{style1,style2: Text}##`. You can also nest styles.
```ntf
##{bold,f1: Important Text}##
##{s12:AA##:12:BB##\u001E##:6:CC##DD}##
```

---

## 3. Semantic Styles
NTF defines several semantic tokens that are mapped to theme colors:

- **Status**: `success`, `warn`, `error`, `fail`, `danger`, `info`.
- **Code**: `keyword`, `string`, `number`, `boolean`, `operator`, `separator`, `comments`.
- **UI/CLI**: `option`, `input`, `user_input`, `var`, `config`.

Example:
```ntf
##:error: Critical Failure##
##:keyword: public## ##:keyword: class## MyClass {}
```

---

## 4. Verbatim and Code Blocks

### 4.1 Inline Verbatim
Text wrapped in single backticks `` ` `` is treated as literal text.
```ntf
`# This is literal #`
```

### 4.2 Code Blocks
Triple backticks define a code block, optionally with a language for syntax coloring.
````ntf
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```
````
Supported languages include: `xml`, `json`, `java`, `sh` (shell).

---

## 5. Includes
NTF supports including other files using the `!include` directive within a verbatim block.

> [!IMPORTANT]
> The `!include` directive is **not supported in standard output** by default. It must be explicitly configured in the NTF renderer/parser environment to be active.

```ntf
```!include classpath:/path/to/file.ntf```
```

