---
title: NTrace, the output companion
---


# `NTrace` — Conditional Trace Output Utility

`NTrace` is a structured output utility in **Nuts** used to emit **optional diagnostic or trace information** to the standard output stream. It behaves like `NOut`, but only prints output when **tracing is explicitly enabled** in the current session.

---

## 🔍 When to Use `NTrace`

Use `NTrace` to provide **optional messages** that :
- Help during development or debugging,
- Provide insights into internal steps,
- Are not critical and should not mix with standard output (NOut) or error messages (NErr).

Unlike `NOut`, `NTrace` output is optional and controlled by the trace flag, so it won’t clutter output if tracing is turned off.


## Output Destination

> **Note:**  
> `NTrace` writes to `NSession.out()` — the same output stream used by `NOut`.  
> In contrast, `NErr` writes to `NSession.err()`.

This means trace messages can be redirected, styled, and formatted consistently with standard output, but only appear when tracing is active.

---

## Trace Mode Behavior
Trace is enabled by default.

To disable trace output, users can:

- Pass --trace=false or --!trace on the command line:

```bash
nuts --trace=false my-app 
nuts --!trace my-app 
```

- Programmatically disable it in the session:

```java
NSession.of().setTrace(false);
```

When trace is disabled, all calls to NTrace.println(...) are ignored silently.


---

## Example Usage

```
Nuts.require();

// This will print only if trace is enabled (default is true)
NTrace.println("Loading configuration from default path...");
```

## Features (Same as NOut)

`NTrace` supports the complete feature set of `NOut`, including:

- NTF formatting for colors and styling,
- Structured rendering (e.g., JSON, YAML, XML, TSON, table, tree),
- Formatted messages using NMsg,
- Integration with the current session’s output configuration.


## Best Practices
Use NTrace to display less relevant or verbose messages that:

- Are helpful for end users who want to better understand what the tool is doing,
- Should not appear during normal usage but may provide useful context when verbosity is desired (e.g., progress steps, skipped actions, fallback behavior),
- Can be safely ignored without impacting the understanding of the main output.


> **Note:**  
> `NTrace` is not a developer logging mechanism.
> For internal developer-oriented logging, use NLog.
