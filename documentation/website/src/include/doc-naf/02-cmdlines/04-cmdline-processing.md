---
title: Processing the commandline
---

## Using CommandLine, The recommended way...

`NCmdLine` has a versatile parsing API. The **Matcher** is the recommended
entry point: it lets you declare, in a single fluent chain, how each option
or positional argument should be recognized (`when*`), how a match should be
interpreted (`as*`), and how the overall pass should behave once every token
has been considered (`require`/`requireAll`/`anyMatch`/`noMatch`).

```java
NCmdLine cmdLine = NApplication.of().cmdLine(); // or from somewhere else
NRef<Boolean> boolOption = NRef.of(false);
NRef<String> stringOption = NRef.ofNull();
List<String> others = new ArrayList<>();

cmdLine.matcher()
        .when("-o", "--option").asFlag(v -> boolOption.set(v.booleanValue()))
        .when("-n", "--name").asEntry(v -> stringOption.set(v.stringValue()))
        .whenNonOption().asArg(v -> others.add(v.image()))
        .withDefaults()
        .requireAll();

// test if application is running in exec mode
// (and not in autoComplete mode)
if (cmdLine.isCompleteMode()) {
        cmd.printCompleteResult();
    return;
}
//do the good stuff here
NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
```

### The `when*` family — declaring what to match

| Method                                   | Matches                                                 | Notes                                                                                                                                                |
|------------------------------------------|---------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `when(String... names)`                  | One or more named options or positional keywords. | The common case. Supports multi-patterns and multi-word sequences, e.g., `when("install package", "install-package")`. |
| `whenOption()`                           | any token that looks like an option (`-x`, `--xxx`)     | no name filtering                                                                                                                                    |
| `whenNonOption()`                        | any token that does **not** look like an option         | typically used to collect positional arguments                                                                                                       |
| `whenArg(Predicate<NArg> predicate)`     | the single peeked token, filtered by your own predicate | general-purpose replacement for `whenOption`/`whenNonOption` when their fixed shapes aren't enough; safe on an empty cmdline                         |
| `whenRaw(Predicate<NCmdLine> predicate)` | arbitrary, multi-token lookahead                        | the full escape hatch — inspect several upcoming tokens before deciding; **you are responsible for checking `hasNext()`/`peek()` presence yourself** |

### The `as*` family — interpreting a match

| Method                            | Consumer receives    | Value rule                                                                                                                                                                                                       |
|-----------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `asFlag(Consumer<NArg>)`          | matched `NArg`       | boolean toggle; never consumes a following token; honors negation (`--!k`)                                                                                                                                       |
| `asTrueFlag(Consumer<NArg>)`      | matched `NArg`       | like `asFlag`, but the consumer only fires when the flag resolves to `true`                                                                                                                                      |
| `asEntry(Consumer<NArg>)`         | matched `NArg`       | value via `--k=v`, or via a following token — **unless** that token looks like an option, in which case it's left untouched (never errors if absent)                                                             |
| `asAttachedEntry(Consumer<NArg>)` | matched `NArg`       | value **only** via `--k=v`; never inspects or consumes a following token (never errors if absent)                                                                                                                |
| `asRequiredEntry(Consumer<NArg>)` | matched `NArg`       | value via `--k=v`, or **unconditionally** grabs the following token regardless of its shape (so values that start with `-`, like JVM options, are handled correctly); **throws** if no value can be found at all |
| `asArg(Consumer<NArg>)`           | matched `NArg`       | no value contract — hands back whatever matched, as-is (typically paired with `whenNonOption()` or `whenArg(...)`)                                                                                               |
| `asRaw(Consumer<NCmdLine>)`       | the whole `NCmdLine` | full manual control — use for `--help`-style unconditional termination, or conditional termination based on your own runtime check; you decide whether/how much to consume (e.g. `skipAll()`)                    |

### Supplying a raw processor: `with(...)` and `withDefaults()`

`with(NCmdLineProcessor processor)` registers a processor directly, bypassing
the declarative `when`/`as` vocabulary entirely. It's useful for two
different situations:

**1. Falling back to session-level default handling** for anything the
declarative rules above didn't match:

```java
cmdLine.matcher()
        .when("-o", "--option").asFlag(v -> boolOption.set(v.booleanValue()))
        .withDefaults()   // delegates unmatched tokens to NSession defaults
        .requireAll();
```

**2. Mutually-exclusive top-level command dispatch**, where each processor
decides for itself whether it applies (returns `true`) or declines
(returns `false`), and the first one that applies wins:

```java
boolean handled = cmdLine.matcher()
        .when(cl -> doVersion(cl))
        .when(cl -> doInstall(cl))
        .when(cl -> doUninstall(cl))
        .anyMatch();

if (!handled) {
    NOut.println(NMsg.ofPlain("unrecognized command"));
}

// ...

private boolean doInstall(NCmdLine cl) {
    if (!cl.next("install").isPresent()) {
        return false; // not our command — let the next processor try
    }
    String pkg = cl.next().map(NArg::image).orElse("");
    NOut.println(NMsg.ofC("installing %s", pkg));
    return true;
}
```

### Finishing the pass: `require`, `requireAll`, `anyMatch`, `noMatch`

| Method | Returns | Behavior |
|---|---|---|
| `require()` | `void` | tries to match **one** token against every registered rule; **throws** if nothing matched |
| `requireAll()` | `void` | equivalent to `while (cmdLine.hasNext()) { require(); }` — **throws** on the first unmatched token |
| `anyMatch()` | `boolean` | tries every registered rule once against the current token; `true` if one matched, never throws |
| `noMatch()` | `boolean` | `!anyMatch()` |

Use `require`/`requireAll` when an unrecognized token should be a hard error
(the common case for a leaf command). Use `anyMatch`/`noMatch` when you want
to decide yourself what happens on failure — e.g. printing help instead of
throwing.

### A more complete example

```java
NCmdLine cmdLine = NApplication.of().cmdLine();
Opts o = new Opts();

cmdLine.matcher()
        .when("--help").asRaw(cl -> {
            showHelp();
            cl.skipAll(); // everything after --help is discarded
        })
        .when("-f", "--full").asFlag(v -> o.full = v.booleanValue())
        .when("-e", "--example").asEntry(v -> o.example = v.stringValue())
        .when("--color").asAttachedEntry(v -> o.color = v.stringValue())
        .when("-J", "--java-options").asRequiredEntry(v -> o.javaOptions = v.stringValue())
        .whenArg(a -> !a.isOption() && a.isNonOption()
                && (a.image().startsWith("./") || a.image().startsWith("../")))
                .asArg(v -> o.paths.add(v.image()))
        .whenNonOption().asArg(v -> o.positionals.add(v.image()))
        .withDefaults()
        .requireAll();


if (cmdLine.isCompleteMode()) {
        cmd.printCompleteResult();
    return;
}

            //do the good stuff here
            NOut.println(NMsg.ofC("options=%s", o));
```

## Using CommandLine, The low-level way...

The matcher is built entirely on top of `NCmdLine`'s pull API — `next(...)`,
`peek()`, `skip()` — and you can always drop down to that API directly for
full manual control. This is also the form autocomplete support is easiest
to reason about explicitly, since each `next(...)` call can carry its own
display label and value-completion hint right at the call site, instead of
those being attached separately through a matcher chain.

```java
NCmdLine cmd = NApplication.of().cmdLine();
boolean boolOption = false;
String stringOption = null;
List<String> others = new ArrayList<>();

while (cmd.hasNext()) {
    NOptional<NArg> option = cmd.next(NArgType.FLAG, "toggle option", "-o", "--option");
    if (option.isPresent()) {
        boolOption = option.get().booleanValue();
        continue;
    }

    NOptional<NArg> entry = cmd.next(NArgType.ENTRY, "name",
            NArgValueComplete.ofFlags(NArgCompleteFlag.NONE),
            "-n", "--name");
    if (entry.isPresent()) {
        stringOption = entry.get().stringValue();
        continue;
    }

    NOptional<NArg> file = cmd.next(NArgType.ENTRY, "file path",
            NArgValueComplete.ofFlags(NArgCompleteFlag.FILENAMES),
            "--file");
    if (file.isPresent()) {
        others.add(file.get().stringValue());
        continue;
    }

    NOptional<NArg> nonOption = cmd.nextNonOption();
    if (nonOption.isPresent()) {
        others.add(nonOption.get().image());
        continue;
    }

    if (cmd.isCompleteMode()) {
        // still under autocomplete: nothing else matched this word,
        // skip it silently instead of failing the whole completion pass
        cmd.skip();
        continue;
    }

    cmd.throwUnexpectedArgument();
}

// test if application is running in exec mode
// (and not in autoComplete mode)
if (cmd.isExecMode()) {
    //do the good stuff here
    NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
}else{
    cmd.printCompleteResult();
    return;
}
```

Each branch follows the same shape: try one `next(...)` call, and if it's
present, consume it and `continue` the loop; if every branch declines, the
final fallback either silently `skip()`s (when completing — an incomplete
word shouldn't abort the whole completion pass) or `throwUnexpectedArgument()`
(when actually executing — an unrecognized token really is an error).

This is exactly the same decision tree the matcher expresses more
declaratively via `when(...)`/`as*(...)` — the difference is purely how the
per-option display label and completion hint are supplied: inline as
arguments to `next(...)` here, versus attached through the fluent chain
there. Both forms feed the same underlying autocomplete machinery, which is
covered next.

## The Unified Model: Parsing is Completion

One of the most powerful features of NCmdLine is that you do not write separate logic for auto-completion. The same matcher() chain that executes your command also generates shell completions.

### Why do we "process each time"?

When a user presses <TAB> in their shell, the shell temporarily invokes your application with the partial command line up to the cursor (e.g., `myapp --nuts-exec-mode=complete,0,6 --fold`).


- `0` is the argument index and `6` is the offset at that index (the position of <TAB>) 
- `NCmdLine` is instantiated with isCompleteMode() == true.
- The `matcher()` evaluates the rules. Instead of throwing an error on a partial match or a missing value, it operates in "discovery mode".
- It identifies which when conditions partially match, and silently harvests the .display(...) and .valueComplete(...) metadata attached to them.
- cmdLine.printCompleteResult() outputs the gathered candidates to stdout in a format the shell can render.

### Example: Completion-Aware Matching
Notice how `.display()` and `.valueComplete()` are baked directly into the parsing chain. They are ignored during normal execution, but become the source of truth during auto-completion.

```java
cmdLine.matcher()
        .when("--file")
            .display("project file")
            .valueComplete(NArgValueComplete.ofFlags(NArgCompleteFlag.FILENAMES))
            .asEntry(v -> loadFile(v.stringValue()))
        .when("--folder")
            .display("project directory")
            .valueComplete(NArgValueComplete.ofFlags(NArgCompleteFlag.DIRNAMES))
            .asEntry(v -> loadFolder(v.stringValue()))
        .requireAll();

if (cmdLine.isCompleteMode()) {
    cmdLine.printCompleteResult(); // Outputs candidates for "--file" or "--folder"
    return;
}
```

If the user types `myapp --f<TAB>`, the matcher recognizes `--file` as a valid prefix match, harvests "project file" and `FILENAMES`, and suggests it. No separate completion configuration is needed.

### The Low-Level Way: Direct Cursor Control

The Matcher is built entirely on top of NCmdLine's pull API: next(...), peek(), skip(), and pushBack(). You can always drop down to this API directly for full manual control.
This is also the form where autocomplete support is easiest to reason about explicitly, since each next(...) call can carry its own display label and value-completion hint right at the call site.

```java
NCmdLine cmd = NApplication.of().cmdLine();
boolean boolOption = false;
String stringOption = null;
List<String> others = new ArrayList<>();

while (cmd.hasNext()) {
    // 1. Try to match a flag
    NOptional<NArg> option = cmd.next(NArgType.FLAG, "toggle option", "-o", "--option");
    if (option.isPresent()) {
        boolOption = option.get().booleanValue();
        continue;
    }

    // 2. Try to match an entry with a specific completion hint
    NOptional<NArg> entry = cmd.next(NArgType.ENTRY, "name",
            NArgValueComplete.ofFlags(NArgCompleteFlag.NONE),
            "-n", "--name");
    if (entry.isPresent()) {
        stringOption = entry.get().stringValue();
        continue;
    }

    // 3. Try to match a file path
    NOptional<NArg> file = cmd.next(NArgType.ENTRY, "file path",
            NArgValueComplete.ofFlags(NArgCompleteFlag.FILENAMES),
            "--file");
    if (file.isPresent()) {
        others.add(file.get().stringValue());
        continue;
    }

    // 4. Try to match a bare positional argument
    NOptional<NArg> nonOption = cmd.nextNonOption();
    if (nonOption.isPresent()) {
        others.add(nonOption.get().image());
        continue;
    }

    // 5. Fallback: Unrecognized token
    if (cmd.isCompleteMode()) {
        // Still under autocomplete: nothing else matched this word.
        // Skip it silently instead of failing the whole completion pass.
        cmd.skip();
        continue;
    }

    // Exec mode: an unrecognized token is a hard error.
    cmd.throwUnexpectedArgument();
}

// Finalize
if (cmd.isExecMode()) {
    NOut.println(NMsg.ofC("boolOption=%s, stringOption=%s, others=%s", 
        boolOption, stringOption, others));
} else {
    cmd.printCompleteResult();
    return;
}
```

Each branch follows the same shape: try one `next(...)` call, and if it's present, consume it and `continue`. If every branch declines, the final fallback either silently `skip()`s (when completing, because an incomplete word shouldn't abort the pass) or throwUnexpectedArgument() (when executing, because an unrecognized token is a genuine error).
This is exactly the same decision tree the Matcher expresses more declaratively. The only difference is how the per-option display label and completion hint are supplied: inline as arguments to `next(...)` here, versus attached through the fluent chain in the Matcher. Both forms feed the exact same underlying autocomplete machinery.

### ⚠️ Why Branching on isExecMode() is Mandatory

You might wonder: "Why do I have to check isExecMode() or isCompleteMode() at the end? Can't the library just skip the execution logic for me?"
The answer is no, and here is why: Auto-completion is a silent probe. When a user presses <TAB>, their shell temporarily invokes your application in the background with a partial command line.
Your application's job during this probe is to:

- Parse the partial line.
- Gather completion hints (.display(), .valueComplete()).
- Print those hints to stdout via printCompleteResult().
- Exit immediately and cleanly.

If you do not explicitly guard your execution logic with if (cmd.isExecMode()), your application might accidentally perform real side effects (e.g., deleting a file, starting a server, or making a network request) just because the user pressed <TAB> to see what options were available.
By separating the Discovery Phase (Complete Mode) from the Action Phase (Exec Mode), NCmdLine gives you total control, but it requires you to explicitly declare where the action begins.

```java
// ✅ CORRECT: Guard your side effects
if (cmd.isCompleteMode()) {
    cmd.printCompleteResult();
    return; // Exit cleanly after providing hints
}

// 🚀 ACTION PHASE: Only reached if isExecMode() is true
performDestructiveOrRealAction(); 
```