---
title: NLog for elegant Logging
---

# `NLog` — Structured Developer Logging in Nuts

`NLog` is the structured logging engine for the Nuts platform. It unifies semantic operations (`NMsgIntent`), rich ANSI/NTF terminal rendering, thread-scoped context propagation (`NLogScope`), execution metrics, and object-preserving structured messages (`NMsg`).

Because `NMsg` retains raw parameter objects and AST nodes without eagerly flattening them to strings, `NLog` adapts seamlessly to any destination—rendering rich ANSI text for terminal debugging, plain text for rolling files, and fully typed JSON payloads for centralized observability pipelines (ELK, Loki, OpenTelemetry).

## 1. Architectural Overview & Comparison
Classic logging frameworks (JUL, SLF4J, Log4j2) typically interpolate arguments into fixed strings at the log boundary. `NLog` preserves raw objects and metadata inside `NMsg`, allowing the downstream appender or sink to determine the final representation.

```
Traditional Logging (SLF4J / Log4j / JUL):
[Caller] ─── (String template + Args) ───► Eager toString() ───► [Fixed Text Appender]

NLog Pipeline:
[Caller] ───► [NMsg: Raw Objects + AST + Intent + Metrics]
│
├──► Terminal Console  (ANSI Colors & NTF Markdown Styles)
├──► Workspace File    (Clean Plain Text)
├──► JSON / Collector  (Structured Key-Value Payload)
└──► SLF4J Bridge      (Location-Aware Delegation)
```

### Feature Comparison
| Capability                   | Standard Java Logging (JUL)      | SLF4J / Log4j2                      | Nuts `NLog`                                              |
|:-----------------------------|:---------------------------------|:------------------------------------|:---------------------------------------------------------|
| **Object Preservation**      | Discarded after formatting       | Flattened or requires JSON wrappers | **Retained in `NMsg` parameters**                        |
| **JSON / Structured Output** | Requires external appenders      | Requires Jackson/Logstash encoders  | **Native (objects preserved in AST)**                    |
| **Message Styling**          | Raw text only                    | Manual ANSI escape sequences        | **Native NTF (colors, backticks, styles)**               |
| **Context Propagation**      | Thread-bound `MDC` (String-only) | Thread-bound `MDC`                  | **`NLogScope` (prefixes, objects, custom sinks)**        |
| **Operation Semantics**      | Tied to level (`INFO`, `WARN`)   | Unstructured markers                | **First-class `NMsgIntent` (`START`, `CACHE`, etc.)**    |
| **Workspace Awareness**      | Static per ClassLoader           | Static per ClassLoader              | **Contextual per workspace/session via `NSession.of()`** |
| **Template Reusability**     | None                             | Limited                             | **Reusable `NMsgBuilder` streams**                       |


## 2. Basic Usage
Obtain an NLog instance using a class reference or category name:

```java
public class ServiceRunner {
    private /* not static*/ final NLog LOG = NLog.of(ServiceRunner.class);

    public void execute(String path, long timeoutMs) {
        LOG.info(NMsg.ofC("Starting worker on path %s", path));

        try {
            // Task execution
            LOG.debug(NMsg.ofC("Polling resource with timeout %d ms", timeoutMs));
        } catch (Exception ex) {
            LOG.error(NMsg.ofC("Failed to execute worker on %s", path)
                .withThrowable(ex)
            );
        }
    }
}
```

### Direct Log Levels

Convenience methods accept NMsg instances or lazy Supplier<NMsg> lambdas:

- `LOG.info(NMsg msg)`
- `LOG.warn(NMsg msg)`
- `LOG.error(NMsg msg)`
- `LOG.debug(NMsg msg)`
- `LOG.log(Level level, Supplier<NMsg> msgSupplier)`

### Output Destinations

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


## 3. Semantic Logging with NMsgIntent

Logging levels (`SEVERE`, `WARNING`, `INFO`, `CONFIG`, `FINE`, `FINER`, `FINEST`) communicate severity, while `NMsgIntent` classifies operational meaning.

```java
log.log(NMsg.ofC("Loading package descriptor for %s", packageId)
    .withLevel(Level.FINE)
    .withIntent(NMsgIntent.READ)
    .withDuration(NDuration.ofMs(123))
);
```

### Standard NMsgIntent Values

- `NMsgIntent.START` / `SUCCESS` / `FAIL` — Lifecycle boundaries of tasks and operations.
- `NMsgIntent.INIT` / `DISPOSE` — Subsystem setup and resource disposal.
- `NMsgIntent.ALERT` / `NOTICE` — Warnings, notices, and operational flags.
- `NMsgIntent.READ` / `UPDATE` / `ADD` / `REMOVE` — State and file mutations.
- `NMsgIntent.CACHE` — Cache lookups, hits, misses, and evictions.
- `NMsgIntent.PROGRESS` — Batch increments and long-running iterations.
- `NMsgIntent.RUN` — Subprocess or CLI command execution.
- `NMsgIntent.SETTINGS` / `PARSING` — Configuration loading and AST parsing.

Define custom intents as needed: `NMsgIntent.of("SYNC_EVENT")`.

## 4. Scoped Logging (NLogScope) vs. MDC

While standard MDC stores flat key-value pairs in a single ThreadLocal map, NLogScope provides a composable, nested execution model that can append structured prefixes, supply lazy values, and redirect logging output entirely.

```java
public class ScopeDemo {
    public static void main(String[] args) {
        NLogScope rootScope = NLogScope.of()
                .withMessagePrefix(NMsg.ofC("[AppEngine]"))
                .withPlaceholder("module", "ArtifactResolver")
                .withPlaceholder("user", "developer");

        NLog.runInScope(rootScope, () -> {
            // Inherits rootScope variables
            Worker.process();

            // Nested child scope
            NLogScope nestedScope = NLogScope.of()
                    .withMessagePrefix(NMsg.ofC("[Nested:%s]", NMsg.placeholder("module")))
                    .withPlaceholder("transactionId", "TX-9041")
                    .withLog(msg -> NOut.println(NMsg.ofC("[INTERCEPTED] %s", msg)));

            NLog.runInScope(nestedScope, () -> {
                Worker.process();
            });
        });
    }
}

class Worker {
    private static final NLog SCOPED_LOG = NLog.ofScoped(Worker.class);

    public static void process() {
        SCOPED_LOG.log(NMsg.ofC("User %s triggered action in %s",
                NMsg.placeholder("user"),
                NMsg.placeholder("module")
        ));
    }
}
```

### Execution Helpers

- `NLog.runInScope(NLogScope scope, Runnable runnable)` — Executes a block within the given scope context.

- `NLog.callInScope(NLogScope scope, NCallable<T> callable)` — Executes a task within the scope and returns the computed result.


## 5. Object Preservation & JSON Structured Logging

Because `NMsg` stores original argument references (`params()`, `vars()`), log events are not trapped in flattened strings. In addition to human-readable console rendering, `NLog` records can be serialized directly into structured formats like JSON.

```java
public class DeploymentService {

    public void deploy(PackageId pkg, DeployConfig config, UserContext user) {
        NLog log = NLog.of(DeploymentService.class);

        // Parameters retain their raw types (PackageId, DeployConfig, UserContext)
        log.info(NMsg.ofV("Deploying package $pkg for user $user",
                "pkg", pkg,
                "config", config,
                "user", user
        ).withIntent(NMsgIntent.RUN));
    }
}
```

### Multi-Channel Rendering from a Single Call

- Terminal Output (NTF/ANSI):

```
[RUN    ] Deploying package my-app:1.0.0 for user admin
```

- Structured JSON Sink:

```json
{
  "timestamp": "2026-08-22T10:15:30.124Z",
  "level": "INFO",
  "intent": "RUN",
  "logger": "com.company.DeploymentService",
  "message": "Deploying package my-app:1.0.0 for user admin",
  "context": {
    "pkg": {
      "group": "net.company",
      "name": "my-app",
      "version": "1.0.0"
    },
    "user": {
      "id": 42,
      "username": "admin",
      "role": "SUPERUSER"
    }
  }
}
```

## 6. Logger Lifecycle & Workspace Awareness

In Nuts, applications, tools, and extensions execute within workspace sessions. Logger instances resolve their active configuration, log thresholds, and appenders dynamically from the active runtime context.

>> Avoid static final Loggers

```java
// ❌ NOT RECOMMENDED for workspace-aware code:
// Eagerly binds to the class-loading workspace and misses dynamic context switches.
private static final NLog LOG = NLog.of(ServiceRunner.class);

// ✅ RECOMMENDED: Resolved on demand or scoped
public class ServiceRunner {

    public void execute(String path) {
        // Option A: Context-aware resolution (automatically resolves workspace via NSession.of())
        NLog log = NLog.of(ServiceRunner.class);
        log.info(NMsg.ofC("Starting worker on %s", path));

        // Option B: Scoped resolution (inherits active NLogScope prefixes, sinks, & variables)
        NLog scopedLog = NLog.ofScoped(ServiceRunner.class);
        scopedLog.debug(NMsg.ofV("Processing $path", "path", path));
    }
}

```

## 7. Message Formatting Styles (NMsg)

NLog does not force a single placeholder convention. You can pick whichever syntax best fits the context without losing type safety or formatting capabilities:

```java
NLog log = NLog.of("worker");

// C-Style printf formatting (%s, %d)
log.info(NMsg.ofC("Processed %d artifacts in %s", count, path));

// Named Variables / JSON-ready maps ($var)
log.debug(NMsg.ofV("Connecting to $host:$port as $user",
    "host", host, "port", port, "user", user));

// Java MessageFormat ({0}, {1})
log.warn(NMsg.ofJ("Artifact {0} deprecated since version {1}", artifactId, version));

// SLF4J-Style ({})
log.info(NMsg.ofS("Downloading {} to {}", artifactId, targetPath));

// NTF Rich Styling (renders ANSI in console, stripped cleanly in plain files)
log.info(NMsg.ofNtf("Executing ```sh nuts install``` for package **%s**", pkgName));
```


## 8. Batch & Stream Logging with NMsgBuilder

`NMsgBuilder` provides a mutable, reusable template that retains default intents, log levels, and timing metrics across multiple emitted log lines.

```java
NLog log = NLog.of("boot");

// 1. Configure the base template
NMsgBuilder mstart = NMsgBuilder.of()
    .withLevel(Level.CONFIG)
    .withIntent(NMsgIntent.START);

// 2. Reuse template across multiple lines
log.log(mstart.withMsgPlain("==============================================================================="));

String ntfHeader = NIOUtils.loadString(getClass().getResourceAsStream("/standard-header.ntf"), true);
ntfHeader = ntfHeader.replace("${nuts.workspace-runtime.version}", Nuts.version().toString());

for (String line : ntfHeader.split("\n")) {
    log.log(mstart.withMsgNtf(line));
}

log.log(mstart.withMsgPlain(" "));
log.log(mstart.withMsgPlain("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
log.log(mstart.withMsgPlain(" "));

// 3. Emit formatted execution command with NTF tags
log.log(mstart.withMsgC("start ```sh nuts``` %s at %s", 
    Nuts.version(), 
    CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(data.initialBootOptions.creationTime().get())
));
```


## 9. Runtime Configuration (NLogConfig)

Diagnostic logging is strictly separated from standard terminal output (NOut), user errors (NErr), and user trace streams (NTrace).

```java
// Adjust active runtime thresholds
NLog.termLevel(Level.FINE); // Console log threshold
NLog.fileLevel(Level.INFO); // File log threshold
```

Workspace log files are configured via NLogConfig:

| Property         | 	Setter                         | Purpose                                                |
|------------------|---------------------------------|--------------------------------------------------------|
| Terminal Level   | 	logTermLevel(Level)	           | Minimum level written to stderr (Level.OFF by default) | 
| File Level       | 	logFileLevel(Level)	           | Minimum level written to rolling log files             | 
| File Size        | 	logFileSize(int mb)	           | Log rotation size threshold in Megabytes               | 
| File Count       | 	logFileCount(int count)	       | Number of rotated log archives to retain               | 
| File Name        | 	logFileName(String pattern)	   | Name pattern for rotated files (e.g., nuts-%g.log)     | 
| Base Directory   | 	logFileBase(String path)       | Custom target directory for log files                  | 

## 10. SLF4J Bridge & Location Awareness

When an SLF4J binding (Logback, Reload4j, SLF4J Simple) is present on the classpath, Nuts registers Slf4JNLogFactorySPI automatically (via `net.thevpc.nuts:nuts-slf4j` dependency)

- Location Awareness: Delegates via SLF4J's LocationAwareLogger to preserve accurate class names, methods, and line numbers.
- Stack Inspection Fallback: Inspects call stacks to resolve caller metadata when writing to custom log handlers.
- Formatted Envelope: Formats intent tags and durations cleanly for SLF4J string appenders:

```
[START  ] Resolving dependencies for net.thevpc.nuts:nuts (duration: 124ms)
[SUCCESS] Workspace initialized successfully
```


## Summary

- Use NLog for developer-focused internal logs.
- Use NOut/NTrace for user-facing messages.
- Attach semantic verbs (NLogIntent) to add context.
- Use NMsg for structured, styled messages.
- Use .with() for flexible, readable, builder-style logging.

This makes NLog an ideal logging facility for modular, structured, and contextual diagnostics in the Nuts platform.


