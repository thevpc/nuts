---
id: nutsApp
title: Your first Application using nuts
---

# Building Applications with Nuts Application Framework (NAF)

This guide walks through packaging and running a Java application with the `nuts` package manager, then progressively adopting the **Nuts Application Framework (NAF)** to get lifecycle hooks, structured command-line parsing, and shell completion — all from the same codebase.

---

## 1. The problem NAF solves

A plain Maven project with runtime dependencies is not directly executable. The usual fixes — `maven-shade-plugin`, `maven-assembly-plugin`, manually editing `META-INF/MANIFEST.MF` — all require baking a specific packaging strategy into the build, and none of them give you application lifecycle hooks (install/update/uninstall) or shell completion for free.

`nuts` sidesteps this: it resolves dependencies and the main class at **run time**, from the artifact's own metadata, so a standard `mvn clean install` output is already runnable.

### 1.1 Generate a project

```bash
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeArtifactId=maven-archetype-simple -DarchetypeVersion=1.4 -DinteractiveMode=false
```

### 1.2 Add a dependency

```xml
<dependency>
    <groupId>jexcelapi</groupId>
    <artifactId>jxl</artifactId>
    <version>2.4.2</version>
</dependency>
```

### 1.3 Write the app

```java
package com.mycompany.app;

import java.io.File;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class App {
    public static void main(String[] args) {
        try {
            WritableWorkbook w = Workbook.createWorkbook(new File("any-file.xls"));
            System.out.println("Workbook just created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
```

### 1.4 Build and run — no shading required

```bash
mvn clean install

nuts install com.mycompany.app:my-app
nuts my-app
```

`nuts` detects, resolves, and downloads dependencies at run time. The app is installed from the local Maven repository; deploy it to a public repository to make it accessible elsewhere. You can also skip installation entirely and run a jar directly:

```bash
nuts -y com my-app-1.0.0-SNAPSHOT.jar
```

If a jar defines multiple `public static void main` classes, `nuts` prompts for which one to run, interactively.

---

## 2. Opting into NAF

NAF adds install/update/uninstall hooks, structured command-line parsing, and shell completion support. Enable it by adding `nuts` as a compile dependency and flagging the artifact:

```xml
<dependency>
    <groupId>net.thevpc.nuts</groupId>
    <artifactId>nuts</artifactId>
    <version>{{runtimeVersion}}</version>
</dependency>
```

```xml
<properties>
    <nuts.application>true</nuts.application>
</properties>
```

`nuts.application=true` is optional but recommended: it lets `nuts` recognize a NAF app from the remote POM *before* downloading the jar.

---

## 3. Application lifecycle hooks

NAF dispatches to your class based on the invocation mode. Each mode has a dedicated annotation, all optional except `@NApp` + `@NAppRun`:

| Annotation    | Invoked when…                                              | Required |
|---------------|--------------------------------------------------------------|:--------:|
| `@NApp` | Marks the class as a NAF application entry point            | Yes |
| `@NAppRun`    | Normal execution (`nuts my-app ...`)                         | Yes |
| `@NAppComplete` | Shell requests completion candidates (Tab press)              | No |
| `@NAppInstall` | `nuts install ...`                                            | No |
| `@NAppUpdate` | `nuts update ...`                                              | No |
| `@NAppUninstall` | `nuts uninstall ...`                                           | No |

Internally, dispatch is a single switch over the resolved mode:

```java
switch (nApp.mode()) {
    case RUN:        nApp.application().run();                     return;
    case COMPLETE:   /* isolated session, see §5 */                 return;
    case INSTALL:    nApp.application().onInstallApplication();     return;
    case UPDATE:     nApp.application().onUpdateApplication();      return;
    case UNINSTALL:  nApp.application().onUninstallApplication();   return;
}
```

`RUN` and `INSTALL`/`UPDATE`/`UNINSTALL` all execute under the normal, interactive session — these are user-initiated actions, so confirmation prompts and terminal output are expected and appropriate. `COMPLETE` is different: see §5.

---

## 4. A complete example

```java
package com.mycompany.app;

import java.io.File;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

@NApp
public class App {

    public static void main(String[] args) {
        // NApp.builder(args).run() ensures exit codes propagate
        // correctly from exceptions to the calling process.
        NApp.builder(args).run();
    }

    static class Options {
        File file = new File("file.xls");
    }

    @NAppRun
    public void run() {
        NCmdLine cmdLine = NApplication.of().cmdLine();
        Options options = process(cmdLine);
        try {
            WritableWorkbook w = Workbook.createWorkbook(options.file);
            NOut.printf("Workbook just created at %s%n", options.file);
        } catch (Exception ex) {
            ex.printStackTrace(NErr.err().asPrintStream());
        }
    }

    @NAppComplete
    public void complete() {
        NCmdLine cmdLine = NApplication.of().cmdLine();
        process(cmdLine);
        cmdLine.printCompleteResult();
    }

    /**
     * Single command-line walk, shared by run() and complete().
     * NCmdLine already knows its own mode (cmdLine.isCompleteMode()),
     * so the matcher calls behave correctly for both callers without
     * any branching here — this method has no notion of "mode" at all.
     */
    private Options process(NCmdLine cmdLine) {
        Options options = new Options();
        cmdLine.matcher()
                .when("--file").asEntry(a -> options.file = new File(a.stringValue()))
                .when("--fill").asEntry(a -> { /* handle other options here */ })
                .withDefaults()
                .requireAll();
        return options;
    }

    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofC("installing My Application: %s%n", NApplication.of().getId()));
    }

    @NAppUninstall
    public void onUninstallApplication() {
        NOut.println(NMsg.ofC("uninstalling My Application: %s%n", NApplication.of().getId()));
    }

    @NAppUpdate
    public void onUpdateApplication() {
        NOut.println(NMsg.ofC("updating My Application: %s%n", NApplication.of().getId()));
    }
}
```

```bash
nuts -y install com.mycompany.app:my-app
nuts -y uninstall com.mycompany.app:my-app
```

### 4.1 Why `process()` is shared, not duplicated

Shell completion is not a separate feature bolted onto argument parsing — it *is* the same matcher walk, just consulted for candidates instead of run to completion with side effects. Writing two independent parsing bodies for `run()` and `complete()` guarantees they drift: someone adds a flag to `run()`, forgets `complete()` exists, and tab-completion silently stops reflecting reality.

Keeping one `process()` method means:
- Every `.when(...)` block is defined exactly once.
- `run()` and `complete()` differ only in what they do *after* parsing (execute vs. print candidates).
- New options are automatically completion-aware.

### 4.2 Why `run` stays unprefixed

`run()` is left without an `on*Application` prefix deliberately. The `on*` naming is meant to be distinctive enough that a class implementing NAF's interfaces directly (rather than via annotations) doesn't accidentally collide with unrelated method signatures elsewhere. `run` is common vocabulary and doesn't need that protection, since it isn't trying to avoid collision the way the lifecycle hooks are.

### 4.3 Enforcement level

Mode dispatch (§3) routes `RUN` and `COMPLETE` to separate branches before either handler is invoked, so `run()` never executes under completion — there's no `isCompleteMode()` check to add inside it, since it would always be false there. The real risk is narrower but still real: nothing prevents a developer from leaving `@NAppComplete` unimplemented, or implementing `complete()` with logic that doesn't call the same `process()` as `run()`, in which case completion candidates silently diverge from (or omit) what `run()` actually accepts.

If `@NAppComplete` is not implemented, NAF falls back to a no-op: the shell simply receives no candidates at that point. This is the correct default — the alternative (e.g. having the framework try to automatically reuse `run()`'s logic on the developer's behalf) risks executing real side effects during a non-interactive shell callback, which is exactly what the `COMPLETE` session isolation in §5 exists to prevent. A missing completion handler degrades gracefully to "no suggestions"; it does not degrade to "runs the app."

The shared-`process()` pattern is a **convention**, not a compiler-enforced contract — consistent with the rest of NAF's fluent, annotation-driven style (e.g. the `when*`/`as*`/`require`/`anyMatch` matcher vocabulary is agreed-upon shape, not a declarative spec the framework introspects). Document it as the recommended shape for teams adopting NAF; don't rely on the annotation's existence alone to imply safety.

---

## 5. Session isolation during completion

`COMPLETE` mode is invoked non-interactively by the shell — on every keystroke or Tab press, with no human reading the output. This makes side effects that are perfectly fine in `RUN` mode actively harmful in `COMPLETE` mode: a confirmation prompt has no one to answer it, and stray terminal output can corrupt the shell's own line redraw.

NAF isolates `COMPLETE` mode by running it under a modified session copy:

```java
case COMPLETE: {
    NSession s = NSession.of();
    s.copy()
            .bot(true)
            .trace(false)
            .confirm(NConfirmationMode.NO)
            .logTermLevel(Level.OFF)
            .runWith(() -> {
                nApp.application().onCompleteApplication();
            });
    return;
}
```

| Setting | Why |
|---|---|
| `bot(true)` | Marks the session as non-interactive/automated. |
| `trace(false)` | Suppresses execution tracing that has no audience. |
| `confirm(NConfirmationMode.NO)` | Never blocks on a confirmation prompt during a shell-driven invocation. |
| `logTermLevel(Level.OFF)` | Forces terminal logging off for this session, regardless of any `--log-term-*` the user has configured for their interactive sessions. |

### 5.1 Why terminal logging specifically matters

Shell dynamic-completion mechanisms (bash `complete -C`, zsh, fish) capture **stdout only** and parse it into candidates. **stderr passes straight through to the terminal** — the same file descriptor the shell is using to redraw the prompt and command line. Logging output on stderr during a completion invocation isn't ignored; it can visibly corrupt the terminal mid-keystroke. `logTermLevel(Level.OFF)` is therefore in the same category as `confirm(NO)`: it removes a channel a human isn't watching but the terminal itself depends on.

`nuts` supports both terminal and file logging independently:

```bash
nuts --log-level-severe my-app   # both term and file
nuts --log-term-severe my-app    # terminal only
nuts --log-file-severe my-app    # file only
nuts --verbose                   # equivalent to --log-finest
```

Nothing is enabled by default. `logTermLevel(Level.OFF)` in the `COMPLETE` branch is therefore a **defensive floor**, not a fix for an active default problem: it guarantees that a user who has enabled `--log-term-*` for their normal interactive use (e.g. while debugging something) doesn't get that verbosity leaking into every subsequent Tab press. File logging (`--log-file-*`) is left untouched by this override — it's independent of the terminal handler, so a developer debugging "why did completion return zero candidates" can still enable `--log-file-finest` and tail the file without any terminal noise.

### 5.2 A known limitation: the bootstrap window

`logTermLevel(Level.OFF)` is applied inside the `COMPLETE` branch of mode dispatch — but `nuts` bootstrap (workspace initialization, early argument parsing, mode resolution itself) necessarily runs *before* NAF knows which mode it's in. If a user has `--log-term-*` set globally, output emitted during this bootstrap window is not covered by the session override, because the override doesn't exist yet at that point in the lifecycle.

In practice this window is narrow — bootstrap is fast, and high term-verbosity during ordinary use is uncommon — but it's worth documenting explicitly rather than assuming `logTermLevel(OFF)` guarantees silence end-to-end. Closing it properly would mean making mode detection happen early enough in bootstrap that the term log handler can consult it directly, since both are effectively answering the same question ("is this a completion invocation?") at two different layers today.

---

## 6. Summary checklist

- [ ] Add `nuts` as a dependency; set `nuts.application=true`.
- [ ] Annotate the class with `@NApp`.
- [ ] Implement `@NAppRun`; delegate parsing to a shared `process()` method.
- [ ] Implement `@NAppComplete` calling the *same* `process()`, then `cmdLine.printCompleteResult()`.
- [ ] Implement `@NAppInstall` / `@NAppUpdate` / `@NAppUninstall` only if needed.
- [ ] Trust NAF's session isolation (`bot`, `confirm(NO)`, `logTermLevel(OFF)`) for `COMPLETE` mode — don't reintroduce interactive prompts or terminal logging inside `onCompleteApplication()`.
- [ ] Remember file logging (`--log-file-*`) is unaffected by completion-mode term suppression and remains available for debugging.
- 