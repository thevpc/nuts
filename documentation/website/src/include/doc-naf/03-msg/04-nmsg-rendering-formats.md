---
title: Text Rendering Formats
---

NMsg</code> NMsg also provides multiple text rendering formats to make messages visually expressive. 
You can use plain messages that print as-is, styled messages to highlight errors, warnings, or  other emphasis, 
code blocks for monospaced text with optional  language hints, and NTF (Nuts Text Format) for lightweight 
markup supporting bold, italic, colors, and other rich formatting. 
These  formats, combined with placeholders, allow NMsg to produce rich,  dynamic, and visually 
informative output in any context.


NTF allows quick and readable text formatting directly in message strings.

```java
NMsg.ofNtf("##bold## ##:/:italic## ```java public class{} ```");
```

you can use Code blocks to render source code with syntax coloring

```java
NMsg.ofPlain("This is a plain message");
NMsg.ofCode("java", "System.out.println(\"Hello NAF\");");
```

you can also force plain text rendering

```java
NMsg.ofPlain("This is a plain message");
```
