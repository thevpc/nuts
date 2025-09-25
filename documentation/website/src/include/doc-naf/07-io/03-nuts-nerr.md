---
title: NErr Standard Error
---


`NErr` is the error-stream counterpart to `NOut`, providing structured, colored, and format-aware error output in the **Nuts** ecosystem.

It writes to the **standard error stream** configured in the current `NSession`, represented by a customizable `NPrintStream`. Like `NOut`, this stream is fully **NTF-aware** and supports a wide range of formats such as JSON, YAML, TSON, XML, tree, and table.


## Key Features

- Delegates to `NSession.err()` (an `NPrintStream`)
- Fully supports **NTF (Nuts Text Format)** for colored and styled messages
- Supports structured output in multiple formats
- Works seamlessly with `NMsg`, `NMutableTableModel`, and `NTreeModel`
- Ideal for logging warnings, errors, diagnostics, and debugging information

---

## Basic Example

```java
Nuts.require();
NErr.println("An error occurred");
```

## Styled Error Messages

You can leverage NTF for expressive and styled output:


```java
NErr.println("##:error:Something went wrong!##");
NErr.println("##:warn:Warning:## Potential issue detected");
NErr.println("##:fxFF0000:Critical failure##");
```

## Structured Error Reporting

Just like with NOut, you can render structured error data using the current session format:

```java
NSession.of().json();
NErr.println(errorList); // errorList = List<ErrorDetail>
```

You can switch to any other supported format (yaml, xml, table, tree, tson, etc.) using:

```java
NSession.of().table();
NErr.println(errorList);
```

## Formatted Diagnostic Messages

Use `NMsg` to build dynamic, strongly typed error messages:

```java
NErr.println(NMsg.of("Task %s failed after %d attempts", "SyncJob", 3));
```

## Use Cases

- Displaying runtime errors or exceptions in a user-friendly way
- Emitting machine-readable diagnostics for automation tools
- Rendering hierarchical error trees or tabular summaries
- Debugging output during CLI tool development

## Summary

`NErr` brings all the expressive power of NOut to the standard error stream. Whether you're showing simple warnings or structured diagnostic trees, `NErr` ensures your error messages are readable, styled, and format-compliant with the Nuts session configuration.


