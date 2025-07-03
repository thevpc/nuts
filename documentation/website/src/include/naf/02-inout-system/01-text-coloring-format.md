---
id: doc1
title: Nuts Text Format
sidebar_label: Nuts Text Format
---


# Nuts Text Format (NTF)

The **Nuts Text Format (NTF)** is a lightweight, expressive markup language designed specifically to enhance command-line interface (CLI) output with rich, portable, and visually appealing formatting. It provides a powerful yet simple syntax that lets developers create colorful, structured, and semantically meaningful text that works seamlessly across different terminal environments and beyond.

## The Need for NTF

Traditional terminal output often relies on plain text or embedded ANSI escape codes to achieve colors and styles. This approach has several drawbacks:

- **Lack of readability:** Raw escape sequences are hard to read and maintain within source code.
- **Poor portability:** Different terminals support different levels of ANSI or control codes, leading to inconsistent rendering.
- **Limited structure:** Plain text and raw ANSI codes do not express document structure well (like lists, tables, sections).

On the other hand, existing markup languages each have their own limitations for CLI contexts:

- **HTML** is rich and flexible but too verbose and requires an HTML viewer, unsuitable for terminals.
- **Markdown** is easy to write and readable but lacks support for dynamic styling and rich terminal features.
- **Man pages (troff/groff)** provide basic terminal help formatting but are complex to author and limited in styling options.

---

## What Makes NTF Unique?


NTF is crafted to fill this gap by being a **terminal-first markup format** that is both **human-readable and machine-processable**, with several key advantages:

- **Readable markup:** NTF syntax uses simple inline markers for colors, font styles, lists, tables, and sections, which are easy to write and understand.
- **Rich formatting:** Supports foreground and background colors, bold/italic/underline, code blocks, bullet and numbered lists, tables, links, and other structured elements.
- **Portability:** NTF content is **independent of the terminal's escape code specifics**. Instead, it is parsed and translated to the appropriate ANSI sequences or other target formats at runtime.
- **Multi-target rendering:** Beyond ANSI terminals, NTF can be converted to **Markdown** (for documentation or developer notes) and **HTML** (for web-based manuals), ensuring a unified authoring experience.
- **Context-awareness:** NTF rendering adapts automatically to the capabilities of the target output device or session configuration, allowing graceful degradation when color or style is not supported.
- **Easy toggling:** Users can enable or disable colored output through standard Nuts options or programmatically, without affecting the underlying markup.

---

## NTF in Nuts Ecosystem

Within the **Nuts** toolbox, NTF plays a central role in delivering a consistent, high-quality user experience:

- **Command-line help system:** All command help, options, examples, and warnings in Nuts are authored in NTF. This allows help to be:

    - **Colorful and well-structured** on capable terminals.
    - **Plain-text friendly** when color is disabled.
    - **Automatically exportable** to Markdown or HTML for documentation portals.

- **Output formatting:** When printing messages, lists, objects, or errors, Nuts can utilize NTF to add semantic structure and emphasis, improving clarity and user comprehension.

- **Unified authoring:** Developers write output messages once in NTF and can be confident it will render correctly across all supported environments without manual adjustments.

---

## How NTF compares to other formats:

NTF is specifically designed for **developer-friendly, portable, terminal-first output formatting**. It bridges the gap between simple text styling (like ANSI escape codes) and more advanced document-oriented formats (like Markdown or AsciiDoctor), making it uniquely suited for CLI applications.

The table below highlights how NTF compares to other common formats across key capabilities:

| Feature                                  | **NTF**        | **ANSI Escape Codes** | **Markdown**    | **AsciiDoctor** | **HTML**       |
|------------------------------------------|----------------|------------------------|------------------|------------------|----------------|
| **Colored output**                       | ✅ Named colors, indexed and hex support | ✅ Manual, code-based | ❌ (Extensions needed) | ✅ With roles/styles | ✅ CSS/inline styles |
| **Styled text**                          | ✅ Bold, italic, underline, strikethrough | ✅ Limited (manual control) | ✅ Bold, italic | ✅ Bold, italic, underline | ✅ Full style control |
| **Semantic color tags** (e.g. `error`, `warning`) | ✅ Built-in mappings (`error:red`, `info:blue`, etc.) | ❌ None | ❌ None | ⚠️ Manual via roles | ✅ Possible via class |
| **Nested styles**                        | ✅ Fully supported (e.g. `error bold`, or `green underline`) | ❌ Complex / fragile | ❌ Not supported | ✅ Supported | ✅ Fully supported |
| **Combined styles**                      | ✅ Easily combined with simple markup | ❌ Manual code combinations | ❌ Limited | ✅ With roles | ✅ With CSS |
| **Structured sections** (titles, subtitles) | ✅ NTF supports semantic headers (`# Title`, `## Subtitle`) | ❌ None | ✅ Basic headings | ✅ Full document structure | ✅ Rich document structure |
| **Lists (bullet, numbered)**             | ✅ Simple syntax (`- item`, `1. item`) | ❌ None | ✅ Yes | ✅ Yes | ✅ Yes |
| **Tables**                               | ✅ Lightweight NTF table syntax | ❌ None | ✅ Basic tables | ✅ Rich tables | ✅ Rich tables |
| **Syntax highlighting** (code snippets)  | ✅ With language tag | ❌ None | ✅ (Limited, via extensions) | ✅ With language tag | ✅ Full, with JS/CSS |
| **Terminal rendering support**           | ✅ Auto-adapts (ANSI, plain, HTML, Markdown) | ✅ Terminal only | ❌ Not terminal-targeted | ❌ Not terminal-targeted | ❌ Not terminal-targeted |
| **Portability across environments**      | ✅ Designed for CLI and convertible to HTML/Markdown | ❌ Terminal only | ✅ Editor/docs only | ✅ Editor/docs only | ✅ Web/browser only |
| **Ease of authoring for CLI output**     | ✅ Very high (compact, readable, intuitive) | ❌ Low (escape-heavy) | ⚠️ Limited styling | ⚠️ Verbose | ❌ Too verbose for CLI |

---

## Summary

The Nuts Text Format is a **modern, terminal-optimized markup language** that:

- Improves readability and maintainability of CLI output markup.
- Enables richly formatted, colorful, and structured terminal output.
- Supports conversion to Markdown and HTML for seamless documentation.
- Enhances the Nuts ecosystem by unifying CLI and documentation presentation.

NTF represents a thoughtful balance between simplicity, expressiveness, and portability, empowering Nuts users and developers to build sophisticated, professional command-line applications with minimal effort.

---

**```nuts```** comes up with a simple coloring syntax that helps writing better looking portable command line programs.
standard output is automatically configured to accept the "Nuts Text Format" (NTF) syntax. 
Though it remains possible to disable this ability using the --!color standard option (or programmatically, 
see **```nuts```** API documentation). NTF will be translated to the underlying terminal implementation using ANSI 
escape code on linux/windows terminals if available.

Here after a showcase of available NTF syntax.

![text-coloring-format](assets/images/console/text-coloring-format-01.png)


![text-coloring-format](assets/images/console/text-coloring-format-02.png)


![text-coloring-format](assets/images/console/text-coloring-format-03.png)


![text-coloring-format](assets/images/console/text-coloring-format-04.png)

# Nuts Text Format Specification

```
<TOKEN> S10: '##########'
<TOKEN> S9 : '#########'
<TOKEN> S8 : '########'
<TOKEN> S7 : '#######'
<TOKEN> S6 : '######'
<TOKEN> S5 : '#####'
<TOKEN> S4 : '####'
<TOKEN> S3 : '###'
<TOKEN> S2 : '##'
<TOKEN> S1 : '##'
<TOKEN> A3 : '\```'

<RULE>  S2 ':' KEY ':' ANYTHING S2
<RULE>  S2 '{:' WORD ANYTHING S2
<RULE>  13 ANYTHING A3

```
