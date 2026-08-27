---
title: Styling Messages
---

In NAF, messages are not just plain text — they can be styled and formatted to convey meaning, emphasize content, or improve readability. 
The NMsg API allows you to combine colors, text modes, and semantic tokens to create rich, dynamic messages that adapt to different contexts (CLI, GUI terminals, logs, etc.).

Why style messages?

- Highlight important information: e.g., warnings, errors, success messages.
- Improve readability: visually distinguish values, keys, or code snippets.
- Semantic clarity: convey the role of a message part (like a keyword, boolean, or comment) rather than just its content.
- Consistency: pre-defined color schemes and semantic tokens help maintain a unified look across your application.


# Styling Categories

## Default Styling
NAF automatically applies default styles to many common data types, so messages are expressive without requiring explicit styling:
- Boolean values (true / false) are styled using NTextStyle.bool().
- Numbers
- Dates / Times / Temporals
- Enums
- etc.

```java
// Boolean value without explicit styling
NOut.println(NMsg.of("Value=%s", true));

// Equivalent to explicitly styling the boolean
NOut.println(NMsg.of("Value=%s", NMsg.ofStyledBool("true")));
```


## Color Index / Theme
NAF provides predefined colors, e.g., primary1, secondary5, error, warn, which map to your application’s theme. These ensure consistent appearance without manually specifying RGB values.

```java
// Primary / Secondary themed colors
NMsg.ofStyledPrimary1("text");
NMsg.ofStyledSecondary5("text");

// Arbitrary foreground color
NMsg.ofStyledForegroundColor("text", Color.RED);

// Modes: bold, blink, striked
NMsg.ofStyledBold("text");
NMsg.ofStyledBlink("text", Color.RED);
NMsg.ofStyledStriked("text");
```


## Foreground / Background Colors 
You can specify arbitrary colors using Java Color objects. Foreground colors affect text color; background colors can be combined to create highlighted blocks or banners.

```java
// Arbitrary foreground color
NMsg.ofStyledForegroundColor("text", Color.RED);
```

## Text Modes 
Modes like bold, italic, blink, strikethrough add emphasis and can be combined with colors for richer visual cues.

```java
// Modes: bold, blink, striked
NMsg.ofStyledBold("text");
NMsg.ofStyledBlink("text", Color.RED);
NMsg.ofStyledStriked("text");
```

## Semantic Tokens 
These are high-level categories representing the meaning of text:
- comments → for secondary or muted content
- warn → for warnings
- error → for errors or alerts
- keyword, string, boolean → for syntax-like highlighting

```java
NMsg.ofStyledComments("comment");
NMsg.ofStyledWarn("warning");
NMsg.ofStyledString("string");
NMsg.ofStyledKeyword("keyword");
NMsg.ofStyledBoolean("boolean");
NMsg.ofStyledError("error");
```
