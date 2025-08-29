---
title: NLog for elegant Logging
---

# `NLog` — Structured Developer Logging in Nuts

`NLog` is the structured, developer-oriented logging API in **Nuts**, designed for internal application diagnostics, debugging, and traceability. It complements user-facing output tools like `NOut` and `NTrace`, offering **fine-grained control**, **customizable log levels**, and **semantic logging verbs**.


---

## 🔧 Basic Usage

You can obtain a logger using:

```java
NLog log = NLog.of(MyClass.class);
```

Then log messages with a semantic level:

```java
log.error(NMsg.ofC("[%s] not yet supported", featureName));
log.warn(NMsg.ofC("Slow lock file: waited %s on %s", duration, path));
```


Log methods include:
- `log.info(...)`
- `log.warn(...)`
- `log.error(...)`
- `log.debug(...)`
- `log.trace(...)`

All accept an `NMsg`, or lazily supplied via `Supplier<NMsg>`.


## Structured Logging with Verbs and Levels

Each log message can include:

- A standard log level: `SEVERE`, `WARNING`, `INFO`, `CONFIG`, `FINE`, `FINER`, `FINEST`, etc.
- A semantic verb (e.g., `READ`, `START`, `FAIL`, `CACHE`, etc.) to describe the nature of the event.


## Generic Log Example

```java
log.log(Level.INFO, NLogVerb.ADD, NMsg.ofPlain("Added new configuration entry"), null);
```

```java
log.log(
    Level.FINE,
    NLogVerb.UPDATE,
    () -> NMsg.ofC("Updated setting: %s", key),
    () -> null
);
```

## Common NLogVerb Values

`NLogVerb` adds semantic meaning to logs beyond plain text:

- `INFO`	Informational messages
- `DEBUG`	Debug-level messages
- `START`	Start of an operation
- `SUCCESS`	Successful end of an operation
- `FAIL`	Operation failure
- `WARNING`	Warnings
- `READ`	Read/access events
- `UPDATE`	State or file updates
- `ADD`	Resource addition
- `REMOVE`	Resource removal
- `CACHE`	Cache-related operations
- `PROGRESS`	Ongoing task progress

You can define custom verbs using `NLogVerb.of("CUSTOM_VERB")`.


## Fluent Logging with NMsg

For advanced logging, use the fluent builder API with `.withXYZ()` :

```java
log
    .log(
            NMsg.ofC("[%s] %s", action, NCmdLine.of(context.getCommand()))
            .withLlevel(Level.FINER)
            .withIntent(NMsgIntent.START)
        );
```


Supports:

- Setting level (withLevel(...))
- Adding a verb (withIntent(...))
- Attaching exceptions (withThrowable(Throwable))
- Attaching per uration (withDurationMs(Throwable))
- Final logging (log(...))

## Example with Exception

```java
NLog.of(Nsh.class)
    .log(NMsg.ofC("Error resolving history file: %s", history.getHistoryFile())
        .withError(ex)
    );
// equivalent
NLog.of(Nsh.class)
    .log(NMsg.ofC("Error resolving history file: %s", history.getHistoryFile())
        .withLevel(Level.SEVERE)
        .withIntent(NMsgIntent.FAIL)
        .withThrowable(ex)
    );
```

## Output Destinations

By default, NLog messages are rendered to:

- Standard error (NSession.err()), so they remain separate from user output.
- And/or a workspace log file, depending on the session configuration.

This ensures that logs:
- Do not pollute standard output (NOut, NTrace),
- Can be persisted and analyzed later if needed,
- Can be redirected or filtered using Nuts session capabilities.

:::tip
You can customize log destinations by configuring the NSession, such as redirecting logs to files, suppressing error output, or adjusting verbosity.
:::

## Why NLog + NMsg Is Unique
In most Java logging frameworks:
- Message formatting is tied to string interpolation styles (e.g., SLF4J uses {} placeholders, Java Logging uses {0}, Log4j2 can use %s, etc.).
- You must format messages manually for each logging backend, or risk inconsistent logs.
- Structured output (e.g., JSON logs, table views) requires extra work or third-party wrappers.

With NLog, this complexity disappears:
You write once with NMsg:

```java
log.warn(NMsg.ofC("File %s could not be read", path));
```

Or:

```java
log.info(NMsg.ofJ("User {0} logged in at {1}", username, timestamp));
```

Or with variables:

```java
log.debug(NMsg.ofV("Downloading $file to $path", NMaps.of("file", id, "path", target)));```
```

Then the rendering adapts to the output:
- CLI (NTF) → colored and styled
- File → plain text
- JSON → structured representation
- GUI → rich visual log viewers
- Remote API → formatted strings or JSON


## Summary

- Use NLog for developer-focused internal logs.
- Use NOut/NTrace for user-facing messages.
- Attach semantic verbs (NLogVerb) to add context.
- Use NMsg for structured, styled messages.
- Use .with() for flexible, readable, builder-style logging.

This makes NLog an ideal logging facility for modular, structured, and contextual diagnostics in the Nuts platform.


