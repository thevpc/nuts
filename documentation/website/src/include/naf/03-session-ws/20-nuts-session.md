---
title: NSession
---

`NSession` defines the current execution context within a Nuts `NWorkspace`. It encapsulates:

- Command-line options
- User preferences
- Output formatting
- Trace/log verbosity
- Interactive modes
- Runtime state

Every operation within `Nuts` is executed in the scope of a `NSession`.

## Getting the Current Session

### Elegant (fail-fast)

```java
    NSession session = NSession.of();
```

### Safe (optional)

```java
Optional<NSession> opt = NSession.get();
```

Returns an Optional session, or empty if not available.

## What Does a Session Do?

- Holds contextual flags like --trace, --yes, --bot, --dry, --confirm
- Controls output formatting: plain, json, xml, tree, table, etc.
- Configures confirmation/interaction modes
- Tracks fetch/cache strategies and expiration
- Controls repository settings
- Defines runtime identity (root(), sudo(), etc.)

## Thread-Scoped Context
NSession is thread-local and inherited by spawned threads unless explicitly changed.
To run with a different session:


```java
session.runWith(() -> {
    // Executes within the session context
});
```

Or return a result:
```java
String result = session.callWith(() -> computeSomething());
```

## Common Flags and States
-  `--trace`	`isTrace()`	Enables trace-mode output
-  `--yes`	`isYes()`	Assume “yes” for confirmations
-  `--no`	`isNo()`	Assume “no” for confirmations
-  `--ask`	`isAsk()`	Always ask for confirmation
-  `--bot`	`isBot()`	Enable non-interactive/script mode
-  `--dry`	`isDry()`	Dry-run only, no actual execution

## Output Format

```java
Control the rendering format of structured output:
session.json();     // JSON
session.table();    // Tabular
session.tree();     // Tree
session.xml();      // XML
session.props();    // Properties
session.plain();    // Default/plain text
```

Output formats affect rendering of `NOut.println(...)`, logging, tables, etc.

## Streams Access
Each session controls its I/O streams (also accessible via NOut, NErr and NIn):

```java
session.out().println("Standard Output");
session.err().println("Error Output");
session.in().readLine();
```

This enables custom I/O redirection (e.g., GUI, files, remote shells).

## Trace Modes

Trace mode activates auxiliary output useful for end users (not developers):
- `isPlainTrace()`: plain-text trace
- `isIterableTrace()`: structured trace with iterable format
- `isStructuredTrace()`: structured trace without iterable mode

```java
if (session.isTrace()) {
    NOut.println("Tracing enabled...");
}
// same as
NTrace.println("Tracing enabled...");
```


## Dependency Resolution Options And Fetch Strategy
Used when resolving artifacts and loading jars from repositories (dynamic classloading)

- `isTransitive()`	Use transitive repositories
- `isCached()`	Use cached data when possible
- `isIndexed()`	Use indexed metadata
- `getExpireTime()`	Expire cache before this date
- `setFetchStrategy()`	Customize fetch strategy

```java
session.setFetchStrategy(NFetchStrategy.ONLINE);
```

Available strategies:
- ONLINE
- OFFLINE
- CACHE_ONLY
- REMOTE_ONLY


## Confirmation and Interaction
Control how user prompts are handled:

```java
session.yes();              // Force auto-yes
session.no();               // Force auto-no
session.ask();              // Always prompt

session.setConfirm(NConfirmationMode.YES);
```


## Interactive Session Features
Enable/disable progress output:

```java
session.setProgressOptions("auto");
```

## Control GUI/headless:
```java
session.setGui(true);
```

## Customize output line prefixes:

```java
session.setOutLinePrefix("[out] ");
session.setErrLinePrefix("[err] ");
```

## Sample Use Case

```java
NSession session = NSession.of().json().setTrace(true);
List<MyObject> data = ...;
NOut.out(session).println(data);  // will output JSON trace if enabled

```

## Advanced Configuration


You can clone and configure sessions:

```java
NSession childSession = session.copy()
        .setOutputFormat(NContentType.XML)
        .setBot(true)
        .setTrace(false);
        
```


## Redirecting Session Streams (Advanced I/O Control)

Nuts allows you to redirect the I/O streams of a session to memory, files, or custom terminals. This is especially useful for scripting, capturing outputs programmatically, or testing.
You can run a block of code using a customized session that redirects output to memory. 
This is useful for capturing the result of structured rendering (e.g., JSON, XML, table) without printing it to the console.
Example:

```java
String result = NSession.of().copy()
    .setTerminal(NTerminal.ofMem())     // redirect all I/O to memory
    .callWith(() -> {
        NSession.of().json();           // structured output (e.g., JSON)
        NOut.println(Arrays.asList("a", "b", "c"));
        return NOut.out().toString();   // retrieve the rendered output as string
    });
```

Explanation: 

- `setTerminal(NTerminal.ofMem())`: uses an in-memory terminal for all I/O
- `NSession.of().json()`: sets the output format to JSON
- `NOut.println(...)`: renders the list to the output stream
- `NOut.out().toString()`: fetches the printed result from memory

> This technique is useful when you want to render structured output for internal use (e.g., passing to another API or storing in a log file), 
> rather than displaying it directly.


## Log Configuration

Control logging levels and filters:

```java
session.setLogTermLevel(Level.INFO);
session.setLogFileLevel(Level.FINE);
session.setLogFilter(log -> log.getVerb().equals(NLogVerb.FAIL));
```

## Listeners
Register and listen to session/workspace events:

```java
session.addListener(new NWorkspaceListener() {
...
});
```

Supported listener types:
- NWorkspaceListener
- NRepositoryListener
- NInstallListener
- NObservableMapListener

## Best Practices

- Use NSession.of() only when you're sure a session context exists
- Always configure session flags (--yes, --bot, etc.) when parsing application commandlines
- 
