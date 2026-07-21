# A First Tour of NAF: Building NutsAdminCLI, One Nuts Idiom at a Time
This is a first tour of NAF — not a reference, and not a pitch. NAF grew out of building Nuts itself: wherever an existing library was too heavy, did things differently than I wanted, or simply wasn't good enough, I built the piece myself, and over time those pieces became reusable on their own. The result is a large toolkit, and this tutorial deliberately covers a small slice of it — a sample, not an inventory.
The goal is narrow and practical: by the end, you should be able to open Nuts' own source code and not flinch at an unfamiliar NXxx type or an ofXxx() factory call — and you should have a working, standalone command-line tool built with a few of Nuts' power tools along the way. We'll build that tool, NutsAdminCLI, incrementally across ten modules, each one a stop at a different corner of the toolkit: CLI parsing, terminal output, files, structured data, concurrency, process execution, expressions, dynamic loading, and (in the last module) bridging into Spring Boot.
What this tutorial is not: it's not exhaustive (this is a fraction of the API surface), it's not making the case that NAF is the right choice for your project, and it's not a tour of NAF's internal design decisions. If you want that conversation, that's a different document. This one just wants you fluent enough, fast enough, to keep going on your own.

> PLAN

Target Application: We will build a unified system administration and diagnostics CLI tool (NutsAdminCLI).

    Modules 1 through 9 will build this as a pure, standalone NAF application using only the core nuts dependency.
    Module 10 will demonstrate how to take this exact application and integrate it into a Spring Boot context, bridging NAF's native logging to SLF4J.

Module 1: Standalone Bootstrapping and Application Lifecycle

    Objective: Initialize a pure NAF application and manage its lifecycle without any external DI containers.
    Core APIs: Nuts, NApplication, NWorkspace, NSession, @NAppDefinition, @NAppRunner.
    Key Concepts: 
        The standalone nature of NAF (no Spring required).
        Differentiating between NWorkspace (global context) and NSession (thread-local execution).
        Implementing pure Java lifecycle hooks (@NAppInstall, @NAppUpdate, @NAppUninstall).
    Implementation: Configure the Maven pom.xml (with nuts.application=true), implement the main class with a standard public static void main, and define setup/teardown logic triggered by the Nuts package manager.

Module 2: Command-Line Parsing and Argument Management

    Objective: Build a robust, native command-line interface using NAF's stateful parser.
    Core APIs: NCmdLine, NArg, NRef.
    Key Concepts: 
        Non-destructive token parsing (peeking vs. consuming).
        Handling short/long options, clustered flags, and non-option arguments.
        Distinguishing between execution mode and auto-complete mode.
    Implementation: Parse user inputs for the CLI tool, validate arguments, handle unexpected flags gracefully, and implement a standardized --help flow.

Module 3: Native Structured Messaging, Terminal Styling, and Logging

    Objective: Implement rich console output and semantic developer logging using NAF's native APIs.
    Core APIs: NMsg, NOut, NErr, NTrace, NLog, NLogIntent, NTF (Nuts Text Format).
    Key Concepts: 
        Replacing System.out with session-aware streams (NOut, NErr).
        Using NTF markup for portable, colorized terminal output.
        Semantic logging using NLogIntent (e.g., START, FAIL, CACHE) and structured message formatting (ofC, ofJ, ofV).
    Implementation: Create styled diagnostic outputs, render structured data as JSON or Tables based on session flags, and implement scoped, intent-based logging.

Module 4: Filesystem Abstraction and Data Integrity

    Objective: Manage files and resources across local/remote protocols with built-in integrity checks.
    Core APIs: NPath, NCp, NDigest, NStoreType.
    Key Concepts: 
        Unified path abstraction (local, HTTP, classpath, SSH).
        XDG-compliant standard directory resolution (config, cache, log, temp).
        Data integrity via checksums and monitored file transfers.
    Implementation: Resolve the application's configuration directory, download a remote diagnostic script via HTTP, copy it locally using NCp with a progress monitor, and verify its integrity using NDigest.

Module 5: Structured Data Parsing and Serialization

    Objective: Read, manipulate, and write configuration files without external JSON/XML libraries.
    Core APIs: NElement, NElementReader, NElementWriter.
    Key Concepts: 
        Format-agnostic data virtualization.
        Dynamic tree navigation and manipulation of structured data.
    Implementation: Parse a local JSON configuration file into an NElement, modify specific nodes in memory, and serialize the updated configuration back to disk in JSON, XML, or TSON format.

Module 6: Concurrency, Resilience Patterns, and Error Modeling

    Objective: Implement enterprise-grade fault tolerance, state management, and concurrency controls.
    Core APIs: NOptional, NCachedValue, NRetryCall, NSaga, NLock, NProgressMonitor.
    Key Concepts: 
        Tri-state error modeling (PRESENT, EMPTY, ERROR) using NOptional.
        Resilient caching with automatic retries and fallback retention (NCachedValue).
        Compensating transactions for multi-step workflows (NSaga).
        Inter-process and intra-process resource locking (NLock).
    Implementation: Build a resilient network-fetching mechanism that caches results, implement a multi-step diagnostic workflow that rolls back on failure, and use NLock to prevent concurrent execution of the tool.

Module 7: External Process Execution and System Monitoring

    Objective: Execute native OS commands safely and monitor running system processes.
    Core APIs: NExec, NPs.
    Key Concepts: 
        Cross-platform process execution with environment scrubbing and stream redirection.
        Remote execution via SSH.
        OS-level process discovery and termination.
    Implementation: Execute local system diagnostics (e.g., ping, ifconfig), capture and parse their output, list running Java processes using NPs, and gracefully terminate specific background tasks.

Module 8: Runtime Expression Evaluation

    Objective: Implement dynamic, runtime expression parsing for configuration-driven logic.
    Core APIs: NExpr, NExprNode, NExprEvaluator.
    Key Concepts: 
        Type-safe, extensible expression evaluation.
        Declaring custom functions, constants, and variables at runtime.
    Implementation: Allow users to pass mathematical or logical expressions via the CLI (e.g., --eval="sin(x*pi) + threshold"), parse them using NExpr, and evaluate them dynamically against runtime variables.

Module 9: Runtime Dependency Resolution and Dynamic Classloading

    Objective: Dynamically fetch and execute external artifacts at runtime without bundling them.
    Core APIs: Nuts Core Engine (Resolution APIs), NId.
    Key Concepts: 
        Real-time classpath manipulation and isolated classloading.
        Querying Maven-compatible repositories dynamically.
    Implementation: Resolve a third-party utility library from Maven Central at runtime, load it into an isolated classloader, and execute its methods dynamically.

Module 10: Enterprise Integration: Spring Boot & SLF4J

    Objective: Bridge the standalone NAF application into the Spring ecosystem and unify logging with SLF4J.
    Core APIs: nuts-spring-boot, nuts-slf4j, @Import(NutsSpringBootConfig.class).
    Key Concepts: 
        Marrying NAF's deterministic workspace constraints with Spring’s Bean lifecycle.
        Injecting session-scoped context records (NSession, NWorkspace) into Spring singletons.
        Routing NAF's native NLog (with its NMsg formatting and NLogIntent semantics) seamlessly through SLF4J/Logback.
    Implementation: Add the Spring Boot and SLF4J dependencies, annotate the main class, inject NAF components into Spring @Component beans, and demonstrate how an NLog.error() call automatically appears in the Spring Boot structured JSON/Logback output with all its semantic intents intact.

## Module 1: Standalone Bootstrapping and Application Lifecycle
**Objective:** Initialize a pure NAF application, configure its Maven descriptors, and implement managed lifecycle hooks without relying on any external frameworks like Spring.
### Step 1.1: Maven Configuration
To use NAF, we need to add the nuts dependency and inform the Nuts package manager that this artifact is a NAF application. This is done by setting the nuts.application=true property in the pom.xml.
Create a new Maven project and update your pom.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mycompany.admin</groupId>
    <artifactId>nuts-admin-cli</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <dependencies>
        <!-- The core Nuts Application Framework dependency -->
        <dependency>
            <groupId>net.thevpc.nuts</groupId>
            <artifactId>nuts</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        
        <!-- CRITICAL: Tells the Nuts package manager this is a NAF application -->
        <nuts.application>true</nuts.application>
        
        <!-- Optional: Define metadata for the Nuts ecosystem -->
        <nuts.genericName>System Admin CLI</nuts.genericName>
        <nuts.categories>System/Monitoring</nuts.categories>
    </properties>
</project>
```
### Step 1.2: Implementing the Application and Lifecycle Hooks

In NAF, application lifecycle events (Install, Update, Uninstall) are managed by the Nuts package manager. When a user installs or updates your tool via the nuts CLI, Nuts automatically triggers the corresponding lifecycle hooks defined in your code.
We will use NAF's annotation-based approach to define our application and its lifecycle methods.

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;

@NAppDefinition
public class NutsAdminCLI {

    public static void main(String[] args) {
        // Bootstraps the NAF runtime and delegates execution to the @NAppRunner method.
        // NAppRunOptions.ofExit ensures that exit codes are properly propagated to the OS.
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    /**
     * The main execution logic of the application.
     * This is called when the user runs the application normally.
     */
    @NAppRunner
    public void run() {
        // Retrieve the current thread-local execution session
        NSession session = NSession.of();
        
        // Retrieve the application's unique identifier (GroupId:ArtifactId#Version)
        NId appId = NApp.of().id();
        
        NOut.println(NMsg.ofC("Welcome to NutsAdminCLI. Running version: %s", appId.getVersion()));
        NOut.println("Type '--help' for usage instructions. (We will implement this in Module 2)");
    }

    /**
     * Triggered when the application is installed via the Nuts package manager.
     * Use this to create initial configuration files or directories.
     */
    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofStyled(">> Initializing NutsAdminCLI workspace...", NTextStyle.primary1()));
        // We will add actual directory creation using NPath in Module 4
    }

    /**
     * Triggered when the application is updated to a new version.
     * Use this to migrate configuration files or clean up deprecated caches.
     */
    @NAppUpdater
    public void onUpdateApplication() {
        NId appId = NApp.of().id();
        NOut.println(NMsg.ofStyled(">> Updating NutsAdminCLI to " + appId.getVersion() + "...", NTextStyle.warn()));
    }

    /**
     * Triggered when the application is uninstalled.
     * Use this to clean up temporary files or deregister system hooks.
     */
    @NAppUninstaller
    public void onUninstallApplication() {
        NOut.println(NMsg.ofStyled(">> Uninstalling NutsAdminCLI. Cleaning up resources...", NTextStyle.error()));
    }
}
```

### Step 1.3: Understanding the Core Contexts: Workspace vs. Session

Before we compile and run, it is crucial to understand the two foundational concepts of NAF that we just interacted with:

- `NWorkspace` (The Global Context): Think of this as the application's root container (similar to a Spring `ApplicationContext`). It holds global configurations, manages the local Maven-compatible repository cache, and provides access to system-level services. There is typically one active workspace per application execution.
- `NSession` (The Execution Context): This is a lightweight, thread-local context. It encapsulates the runtime state for a specific execution flow: command-line arguments, output formatting preferences (e.g., JSON vs. Table), trace/verbosity flags, and I/O streams. When you call `NSession.of()`, you are retrieving the context for the current thread.

### Step 1.4: Building, Installing, and Testing the Lifecycle
Because Nuts is inherently a package manager, it handles the classpath and dependency resolution dynamically at runtime. We never need to build a "fat JAR" or configure the `maven-shade-plugin` to bundle our dependencies.
However, by setting `<nuts.application>true</nuts.application>` in our POM, we provide a crucial metadata hint to the Nuts package manager. This tells Nuts that our artifact is a full NAF application that supports lifecycle hooks (install, update, uninstall). This allows Nuts to detect and execute these hooks correctly during the installation phase directly from the POM descriptor, without needing to download and inspect the JAR first.

1. Build and deploy to your local Maven repository:
```bash
mvn clean install
```

2. Install the application into the Nuts ecosystem:
Now, we use the nuts CLI to install our newly built artifact. Because of the nuts.application flag, Nuts recognizes it as a NAF application and triggers our @NAppInstall hook.
```bash
nuts install com.mycompany.admin:nuts-admin-cli
```

Expected Output:

```
>> Initializing NutsAdminCLI workspace...
```

3. Run the application:

```bash
nuts nuts-admin-cli
```

Expected Output:

```
Welcome to NutsAdminCLI. Running version: 1.0.0-SNAPSHOT
Type '--help' for usage instructions. (We will implement this in Module 2)
```

4. Update the application (Simulated):
If you were to bump the version in your pom.xml to `1.1.0`, run `mvn install`, and then run `nuts update nuts-admin-cli`, Nuts would trigger the `@NAppUpdater` hook, allowing you to safely migrate user data.

5. Uninstall the application:

```
nuts uninstall com.mycompany.admin:nuts-admin-cli
```

Expected Output:

```
>> Uninstalling NutsAdminCLI. Cleaning up resources...
```

> 💡 **Developer Pro-Tip: Handling Local Cache & Workspace During Development**
> When you are actively coding and recompiling, Nuts might use a cached version of your JAR or workspace state. If you find that your latest changes aren't showing up, or your workspace is in a weird state, you have a few ways to force Nuts to refresh:
>
> 1. **The Reinstall (Simplest):** Just run `nuts reinstall com.mycompany.admin:nuts-admin-cli`. This forces Nuts to refresh the installation of the current version.
> 2. **The Cache Invalidator:** Use the `-N` flag to force Nuts to ignore the local cache and fetch the latest local Maven build: `nuts -N nuts-admin-cli`.
> 3. **Time-Based Invalidation:** You can be even more specific with `-N` by adding a time duration, like `-N10mn` to tell Nuts to invalidate any cached jars older than 10 minutes. This is perfect for rapid development loops!
> 4. **The Version Bump:** Change the version in `pom.xml` and run `nuts update ...`.
> 5. **The Nuclear Option (`-Zy`):** Use `nuts -Zy install ...` to completely reset the workspace. It deletes and recreates the workspace as if it were the very first time running Nuts. **Note the `-y` flag!** Without it, Nuts will ask you to confirm the deletion of every single file in the workspace. The `-y` (auto-confirm) flag bypasses those prompts, making it a true one-command "wipe and restart" tool.


## Module 2: Command-Line Parsing and Argument Management
**Objective:** Build a robust, native command-line interface using NAF's stateful parser (NCmdLine). We will replace standard argument parsing with NAF's fluent API, handle boolean flags, options, and non-option arguments, and properly support shell auto-completion.

### Step 2.1: Understanding NCmdLine
In traditional Java, parsing command-line arguments usually requires external libraries like Picocli or Apache Commons-CLI, or writing brittle for loops over String[] args.
NAF provides `NCmdLine`, a powerful, OS-portable parser that natively supports:

- Short and Long Options: `-v` and `--verbose`.
- Clustered Flags: `-vh` is automatically expanded to `-v` `-h`.
- Boolean Synonyms: `--verbose`, `--verbose=true`, `--verbose=yes`, and even negations like `--!verbose` or `--~verbose`.
- Standard Nuts Options: It automatically recognizes and processes framework-level flags like `--json`, `--trace`, `--yes`, and `--bot` without you having to write a single line of code for them.

### Step 2.2: Retrieving the Command Line and Using `NRef`
Inside a NAF application, you retrieve the parsed command line from the application context. Because Java lambdas require effectively final variables, NAF provides `NRef`, a simple mutable reference wrapper, to hold our parsed values.

```java
// Retrieve the command line for the current application
NCmdLine cmdLine = NApp.of().cmdLine();

// Use NRef to hold mutable state that can be updated inside lambdas
NRef<Boolean> verbose = NRef.of(false);
NRef<String> configPath = NRef.ofNull();
NRef<Boolean> showHelp = NRef.of(false);
List<String> commands = new ArrayList<>();
```

### Step 2.3: The Fluent Matcher API
NAF provides a declarative Matcher API to process arguments. Instead of manually looping and checking tokens, we define a state machine that dictates how different tokens should be handled, and then execute it against the command line.
We will update our `run()` method to parse a `--verbose` flag, a `--config` option, a `--help` flag, and a positional command (like `status` or `check`).

```java
// Retrieve the command line for the current application
NCmdLine cmdLine = NApp.of().cmdLine();

// Use NRef to hold mutable state that can be updated inside lambdas
NRef<Boolean> verbose = NRef.of(false);
NRef<String> configPath = NRef.ofNull();
NRef<Boolean> showHelp = NRef.of(false);
List<String> commands = new ArrayList<>();

// 1. Define the parsing rules (The State Machine)
// We create the matcher ONCE. It configures how tokens should be handled.
cmdLine.matcher()
    // Match boolean flags (-v, --verbose)
    .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
    // Match valued options (-c, --config)
    .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
    // Match help flag
    .with("-h", "--help").matchFlag(v -> showHelp.set(v.booleanValue()))
    // Match non-option arguments (e.g., "status", "check")
    .withNonOption().matchAny(v -> commands.add(v.image()))
    // Handle standard Nuts options (--json, --trace, etc.)
    .withDefaults()
    // 2. Execute the state machine against the entire token stream
    .requireAll(); 
```

Note the elegance of `.requireAll()`. It tells the configured state machine to process the entire token stream until it is empty, completely removing the need for a manual `while(cmdLine.hasNext())` loop in our business logic.


### Step 2.4: Execution Mode vs. Auto-Completion
In traditional CLI development, you are forced to maintain two separate sources of truth:
- Your application's internal Java argument parser.
- A separate, often brittle, shell script (like a Bash complete function or Zsh completion script) that hardcodes the flags and options for the terminal's <TAB> auto-completion.

These two almost always get out of sync. When you add a new flag to your Java app, you have to remember to update the bash script, or the auto-completion breaks.

NAF solves this elegantly by using two modes on the exact same parser. You do not write a separate auto-completion script; the parser is the completion engine. 

- Execution Mode (`isExecMode() == true`): The parser consumes the arguments, maps them to your business logic via the matcher state machine, and executes the app.
- Auto-Completion Mode (`isExecMode() == false)`: When the user presses `<TAB>` in their terminal, the shell secretly invokes your application in the background. NAF uses the exact same matcher state machine (the `.with(...)` rules) to inspect what the user has typed so far, calculates what valid options or arguments are left, and outputs them for the shell to display.

#### The Golden Rule of CLI Safety:####
Because the shell invokes your application in the background when <TAB> is pressed, your code is actually running. This is why checking if (cmdLine.isExecMode()) is absolutely critical before executing your business logic: **you do not want your app to actually execute a command (which might have side effects like writing files, making network calls, or deleting data) just because the user pressed <TAB> to see if a flag existed.**

```bash
// CRITICAL: Check if we are in execution mode (and not in autoComplete mode)
if (!cmdLine.isExecMode()) {
    // We are in auto-complete mode. 
    // The framework has already used our matcher state machine 
    // to calculate and print the valid completions to the shell.
    // We simply return without executing any business logic or side effects.
    return; 
}

// --- Safe to execute business logic below this line ---
```
### Step 2.5: Zero-Boilerplate Help via NTF
In traditional frameworks, writing a `--help` message means hardcoding strings in Java, managing line breaks, and manually aligning columns.
NAF handles this automatically. If you want Nuts to manage the help flow, you simply create an NTF (Nuts Text Format) file named `<groupId-as-path>`/`<artifactId>.ntf` and place it in your `src/main/resources` directory.
When a user runs your application with `--help` (or `-h`), Nuts automatically intercepts the flag, locates this file, parses the NTF markup, and renders it to the terminal with full color and styling support.
1. Create the Help File:

Create the file `src/main/resources/com/mycompany/admin/nuts-admin-cli.ntf`:

`````ntf
```sh nuts-admin-cli``` a System Administration Tool

##) SYNOPSIS:
```sh
nuts-admin-cli [<-options>]... <command>
```
    run administration tool

##) COMMANDS:
```sh
nuts-admin-cli status
```
    Show system status

```sh
nuts-admin-cli check
```
    Run diagnostics
    
```sh
nuts-admin-cli clean
```
    Clear temporary files

##) OPTIONS:

```sh
-v | --verbose
```

```sh
-c | --config <config-file>
```

Specify a custom configuration file


##) NOTE:

##:comment:Standard Nuts options like --json, --trace, and --yes are also supported.##
`````

Because this file exists, Nuts will automatically handle -h and --help. You do not need to parse the help flag in your Java code, and you do not need to write a printHelp() method. Nuts will render this file, apply terminal colors, and exit gracefully.

> (Note: If you ever need dynamic help that changes based on runtime state, you can still intercept --help manually in your matcher, but for 95% of CLI tools, the NTF file approach is the recommended, cleanest path.)

### Step 2.6: Full Implementation

By letting Nuts handle the help flow via the NTF file, our Java code becomes incredibly clean. We remove the showHelp variable, remove the help flag from the matcher, and delete the printHelp() method entirely.
Here is the final, optimized `NutsAdminCLI.java`:
```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {

    public static void main(String[] args) {
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().cmdLine();

        NRef<Boolean> verbose = NRef.of(false);
        NRef<String> configPath = NRef.ofNull();
        List<String> commands = new ArrayList<>();

        // Define the parsing state machine ONCE and execute it.
        // Notice: We DO NOT parse -h/--help here. Nuts handles it automatically 
        // via the com.mycompany.admin.nuts-admin-cli.ntf file!
        cmdLine.matcher()
            .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
            .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
            .withNonOption().matchAny(v -> commands.add(v.image()))
            .withDefaults() // Handles --json, --trace, and automatically intercepts --help
            .requireAll();

        // CRITICAL: Check if we are in execution mode (and not in autoComplete mode)
        if (!cmdLine.isExecMode()) {
            return; // Auto-complete mode; framework handles the rest
        }

        // At this point, if the user passed --help, the app has already printed 
        // the NTF file and exited. We are guaranteed to be in normal execution mode.

        if (commands.isEmpty()) {
            NErr.println(NMsg.ofStyled("Error: Missing command. Use --help for usage.", NTextStyle.error()));
            return;
        }

        String command = commands.get(0);
        
        if (verbose.get()) {
            NOut.println(NMsg.ofStyled("[VERBOSE] ", NTextStyle.primary3())
                .append(NMsg.ofC("Config path: %s | Command: %s", configPath.get(), command)));
        }

        NOut.println(NMsg.ofC("Executing command: ##%s##", command));
    }

    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofStyled(">> Initializing NutsAdminCLI workspace...", NTextStyle.primary1()));
    }

    @NAppUpdater
    public void onUpdateApplication() {
        NId appId = NApp.of().getId();
        NOut.println(NMsg.ofStyled(">> Updating NutsAdminCLI to " + appId.getVersion() + "...", NTextStyle.warn()));
    }

    @NAppUninstaller
    public void onUninstallApplication() {
        NOut.println(NMsg.ofStyled(">> Uninstalling NutsAdminCLI. Cleaning up resources...", NTextStyle.error()));
    }
}
```

### Step 2.7: Testing the CLI
Rebuild and reinstall your application:

```bash
mvn clean install
nuts install com.mycompany.admin:nuts-admin-cli
```

Now, let's test the new features:

```bash
nuts nuts-admin-cli --help
```

**Output:** Nuts automatically finds com.mycompany.admin.nuts-admin-cli.ntf in the classpath, parses the NTF markup, and prints a beautifully colorized, structured help menu to the terminal. No Java code was required to make this happen!

```bash
nuts nuts-admin-cli -vc /etc/myconfig.json check
```

**Output**

```
[VERBOSE] Config path: /etc/myconfig.json | Command: check
Executing command: check
```

3. Auto-Completion Mode:
Because we are using the state-machine matcher and checking ```isExecMode()```, if a user types:
```bash
nuts nuts-admin-cli --con<TAB>
```

```
The shell will invoke the app in the background. Nuts will use the matcher to see that `--config` is a valid, uncompleted option, and suggest it to the user, without executing the check or `status` logic!
```

#### Summary of Module 2
We have successfully built a robust, native command-line interface using NAF's NCmdLine. We utilized the declarative state-machine Matcher API, leveraged NRef for lambda compatibility, and ensured our tool plays nicely with shell auto-completion via isExecMode().
Most importantly, by leveraging Nuts' native NTF help integration, we achieved a zero-boilerplate help system, keeping our Java code strictly focused on business logic.


## Module 3: Structured Messaging, Terminal Styling, and Logging
**Objective**: Replace standard Java I/O with NAF's session-aware streams, build dynamic and styled messages, and implement semantic developer logging. We will also use these new tools to implement the status command for our CLI.

### Step 3.1: Replacing System.out with NOut, NErr, and NTrace

In traditional Java, we use System.out.println(). In NAF, we use NOut, NErr, and NTrace.
Why not System.out? Because NAF's streams are bound to the NSession. If a user runs your app with --json, NOut will automatically format the output as JSON. If they use --table, it renders a table. System.out knows nothing about the session context.

- **NOut**: Standard output stream. Used for user-facing messages and structured data.
- **NErr**: Error output stream. Used for warnings, errors, and diagnostics.
- **NTrace**: Trace/Debug output stream. Crucially, it only prints if the user enables trace mode (e.g., by passing --trace). This is perfect for verbose debugging without cluttering normal output.

```java
// Standard output
NOut.println("System is healthy.");

// Error output
NErr.println("##:error:Failed to connect to database!##");

// Trace output (Only visible if the user runs: nuts nuts-admin-cli --trace status)
NTrace.println("Checking network interfaces...");
```

### Step 3.2: Dynamic Message Formatting with NMsg
Instead of using string concatenation or `String.format()`, NAF provides `NMsg`, a powerful message abstraction that supports multiple formatting styles and integrates seamlessly with `NOut`.

```java
String user = "Alice";
int count = 5;

// 1. C-style formatting (printf-like)
NOut.println(NMsg.ofC("Hello %s, you have %d new tasks.", user, count));

// 2. Java / SLF4J style formatting
NOut.println(NMsg.ofJ("User {} logged in from {}", user, "192.168.1.1"));

// 3. Variable-based formatting (Great for templates and localization)
NOut.println(NMsg.ofV("Threshold is $th, Date is $date", 
    NMaps.of("th", 0.85, "date", LocalDate.now())));

// 4. Moustache-Style Variable-based formatting (Great for templates and localization)
NOut.println(NMsg.ofV("Threshold is $th, Date is {{date}}", 
    NMaps.of("th", 0.85, "date", LocalDate.now())));
```

### Step 3.3: Terminal Styling with NTF (Nuts Text Format)

`NAF` includes `NTF`, a lightweight markup language that allows you to add colors, bold text, and semantic styling directly in your strings. NTF is portable and automatically adapts to the terminal's capabilities (or strips colors if piped to a file).
You can use inline NTF markup:
```java
// Inline NTF markup
NOut.println("##:primary1:Welcome## to ##:bold:NutsAdminCLI##");
NOut.println("##:warn:Warning:## Disk space is low.");
NOut.println("##:error:CRITICAL FAILURE## in module X.");
```

Or use the programmatic API for type-safe styling:
```java
// Programmatic styling using NTextStyle
NMsg styledMsg = NMsg.ofC("Task %s completed with status %s", 
    "Backup", 
    NMsg.ofStyled("SUCCESS", NTextStyle.success())); // Green/Bold
NOut.println(styledMsg);
```

### Step 3.4: Semantic Developer Logging with NLog

While `NOut` is for the end-user, `NLog` is for the developer. Unlike traditional logging frameworks (like `Logback` or `Log4j`) that rely solely on severity levels, NLog provides two unique architectural advantages:

- **Semantic Intents (Verbs)**: Instead of just logging "INFO", you log what happened (e.g., START, SUCCESS, FAIL, CACHE, READ). This makes filtering, monitoring, and structured JSON output incredibly powerful.
- **Scoped Logging (Supercharged MDC)**: You can dynamically inject message prefixes, placeholders, and even custom log handlers into a specific block of code. Any class or method called within that block automatically inherits this context without you having to pass variables around or configure global thread-locals.

1. Semantic Intents and Fluent Logging
Let's update our status command to use semantic intents and the fluent builder API. This allows us to track exactly what the application is doing, and attach metadata like execution duration.

```java

private static final NLog log = NLog.of(NutsAdminCLI.class);

private void executeStatusCommand(boolean verbose) {
    long startTime = System.currentTimeMillis();

    // 1. Log the START of the operation with a semantic intent
    log.info(NMsg.ofC("Executing 'status' command (verbose=%s)", verbose)
            .withIntent(NLogIntent.START));

    try {
        // ... simulate checking CPU and Memory ...
        simulateDelay(500);

        long duration = System.currentTimeMillis() - startTime;

        // 2. Log SUCCESS, attaching the exact duration of the operation
        log.info(NMsg.ofPlain("Status check completed successfully.")
                .withIntent(NLogIntent.SUCCESS)
                .withDurationMs(duration));

    } catch (Exception ex) {
        // 3. Log FAIL, attaching the exception for full stack trace context
        log.error(NMsg.ofPlain("Status check encountered a critical failure.")
                .withIntent(NLogIntent.FAIL)
                .withThrowable(ex));
    }
}
```

2. Scoped Logging (Dynamic Context Injection)
Imagine our `status` command calls three different helper classes (`CpuChecker`, `MemoryChecker`, `NetworkChecker`). 
3. We want every log message generated by these classes to automatically include a `[Diagnostics]` prefix and a `$component` placeholder, without modifying the helper classes or using global `MDC` maps.

> We can achieve this using NLog.runInScope():

```java
private void executeStatusCommand(boolean verbose) {
    // Define a logging scope for the entire diagnostic run
    NLog.runInScope(
        NLogScope.of()
            // Automatically prepend this to every log message in this block
            .withMessagePrefix(NMsg.ofStyled("[Diagnostics] ", NTextStyle.primary3()))
            // Inject a dynamic placeholder available to all logs in this scope
            .withPlaceholder("runId", "RUN-9942"), 
        () -> {
            // --- All logs inside this block inherit the scope! ---
            log.info(NMsg.ofV("Starting diagnostic run $runId..."));
            // Output: [Diagnostics] Starting diagnostic run RUN-9942...
            checkCpu();
            checkMemory();
        }
    );
}

private void checkCpu() {
    // This log automatically gets the "[Diagnostics]" prefix and the $runId placeholder!
    NLog log=NLog.ofScoped(NutsAdminCLI.class); // explicitly pick the scoped log not owned one 
    log.info(NMsg.ofV("Checking CPU for run $runId...")
        .withIntent(NLogIntent.READ));
    // Output: [Diagnostics] Checking CPU for run RUN-9942...
}
```


### Step 3.5: Implementing the `status` Command (Full Integration)
Let's put it all together. We will implement the status command in our `NutsAdminCLI`. This command will simulate checking system components, use NTrace for verbose steps, `NOut` with `NTF` for the final styled report, and NLog for internal developer logging.
Update your `NutsAdminCLI.java` with the new status logic:

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {

    // Initialize the developer logger
    private static final NLog log = NLog.of(NutsAdminCLI.class);

    public static void main(String[] args) {
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().cmdLine();

        NRef<Boolean> verbose = NRef.of(false);
        NRef<String> configPath = NRef.ofNull();
        List<String> commands = new ArrayList<>();

        // State machine for CLI parsing
        cmdLine.matcher()
            .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
            .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
            .withNonOption().matchAny(v -> commands.add(v.image()))
            .withDefaults()
            .requireAll();

        if (!cmdLine.isExecMode()) {
            return; // Auto-complete mode
        }

        if (commands.isEmpty()) {
            NErr.println(NMsg.ofStyled("Error: Missing command. Use --help for usage.", NTextStyle.error()));
            return;
        }

        String command = commands.get(0);

        // Route to the correct command handler
        switch (command) {
            case "status":
                executeStatusCommand(verbose.get());
                break;
            case "check":
                NOut.println(NMsg.ofStyled("Running full diagnostics...", NTextStyle.primary1()));
                break;
            default:
                NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
        }
    }

    /**
     * Implements the 'status' command using NOut, NTrace, NTF, and NLog.
     */
    private void executeStatusCommand(boolean verbose) {
        log.info(NMsg.ofC("Executing 'status' command (verbose=%s)", verbose)
            .withIntent(NLogIntent.START));

        NOut.println(NMsg.ofStyled("=== System Status Report ===", NTextStyle.primary1()));
        NOut.println();

        // 1. Check CPU (Using NTrace for verbose steps)
        NTrace.println("Querying CPU load...");
        simulateDelay(200); // Simulate work
        boolean cpuOk = true; 
        
        // 2. Check Memory
        NTrace.println("Querying Memory usage...");
        simulateDelay(300);
        boolean memOk = false; // Simulate a failure

        // 3. Print Results using NOut and NTF
        NOut.println(NMsg.ofC("CPU Load:    %s", 
            cpuOk ? NMsg.ofStyled("OK", NTextStyle.success()) 
                  : NMsg.ofStyled("HIGH", NTextStyle.error())));
        
        NOut.println(NMsg.ofC("Memory:      %s", 
            memOk ? NMsg.ofStyled("OK", NTextStyle.success()) 
                  : NMsg.ofStyled("CRITICAL", NTextStyle.error())));

        NOut.println();

        // 4. Final Summary
        if (cpuOk && memOk) {
            NOut.println(NMsg.ofStyled("All systems operational.", NTextStyle.success()));
            log.info(NMsg.ofPlain("Status check completed successfully.")
                .withIntent(NLogIntent.SUCCESS));
        } else {
            NErr.println(NMsg.ofStyled("Warning: System requires attention!", NTextStyle.warn()));
            log.warn(NMsg.ofPlain("Status check found critical issues.")
                .withIntent(NLogIntent.FAIL));
        }
    }

    private void simulateDelay(long millis) {
        NConcurrent.of().sleep(millis);
    }

    // ... Lifecycle hooks (@NAppInstall, etc.) remain the same ...
    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofStyled(">> Initializing NutsAdminCLI workspace...", NTextStyle.primary1()));
    }

    @NAppUpdater
    public void onUpdateApplication() {
        NId appId = NApp.of().getId();
        NOut.println(NMsg.ofStyled(">> Updating NutsAdminCLI to " + appId.getVersion() + "...", NTextStyle.warn()));
    }

    @NAppUninstaller
    public void onUninstallApplication() {
        NOut.println(NMsg.ofStyled(">> Uninstalling NutsAdminCLI. Cleaning up resources...", NTextStyle.error()));
    }
}
```


### Step 3.6: Testing the New Features
Rebuild and reinstall your application:
```bash
mvn clean install
nuts install com.mycompany.admin:nuts-admin-cli
```

Now, let's test the different output modes:
1. Normal Execution (User-facing output only):

```bash
nuts nuts-admin-cli status
```
Output:
```
=== System Status Report ===

CPU Load:    OK
Memory:      CRITICAL

Warning: System requires attention!
```

> (Notice how the colors are applied automatically via NTF, and the developer logs (NLog) are hidden from the user).

2. Trace Mode (Revealing the internal steps):
```bash
nuts nuts-admin-cli --trace status
```

Output:

```
Querying CPU load...
Querying Memory usage...
=== System Status Report ===

CPU Load:    OK
Memory:      CRITICAL

Warning: System requires attention!
```

> (Notice how NTrace.println() messages now appear because we passed --trace).

3. JSON Output Mode (The NAF Superpower):

Because we are using `NOut`, if the user passes `--json`, `NAF` automatically intercepts the output format. While our manual `NOut.println` strings won't magically become JSON (they are just strings), if we were to print a Java Object or a List, it would automatically render as `JSON`!

> (In future modules, we will use NElement and NTableModel to print structured data that fully leverages this --json and --table magic).

### Summary of Module 3

We have successfully transitioned from basic System.out to NAF's rich, session-aware I/O ecosystem. We utilized NMsg for dynamic formatting, NTF for beautiful terminal styling, and NLog with NLogIntent for semantic developer logging. We also implemented our first real command (status), cleanly separating user-facing output (NOut) from developer diagnostics (NLog and NTrace).
Next Up: In Module 4, we will dive into Filesystem Abstraction and Data Integrity using NPath, NCp, and NDigest to safely resolve configuration directories, download remote resources, and verify file checksums.


## Module 4: Filesystem Abstraction and Data Integrity
**Objective:** Manage files, directories, and resources across local and remote protocols with built-in integrity checks. We will implement the check command for our CLI, which will resolve standard configuration directories, fetch a remote resource, copy it locally with progress tracking, and verify its checksum.

### Step 4.1: The Problem with java.io.File and java.nio.file.Path

In standard Java, handling files usually involves java.io.File or java.nio.file.Path. While fine for simple local scripts, they fall short in modern, cross-platform applications because:

- They don't understand remote protocols (HTTP, SSH, Classpath).
- They don't know where to safely store configuration, cache, or log files across different operating systems (Windows vs. Linux vs. macOS).

NAF solves this with `NPath`, a unified, protocol-aware path abstraction, and `NStoreType`, which automatically resolves XDG-compliant standard directories.

### Step 4.2: XDG-Compliant Directory Resolution

When building a CLI tool, you need a place to store configuration files. Hardcoding `~/.myapp` works on Linux but fails on Windows (where it should be `%APPDATA%`).
NAF provides `NPath.ofUserStore(NStoreType)`, which automatically maps to the correct, OS-specific standard directory:

```java
// Resolves to:
// Linux:   ~/.config/nuts/default-workspace/config/nuts-admin-cli/
// Windows: C:\Users\<User>\AppData\Roaming\nuts\config\nuts-admin-cli\
// macOS:   ~/.config/nuts/default-workspace/config/nuts-admin-cli/
NPath configDir = NPath.ofUserStore(NStoreType.CONF).resolve("nuts-admin-cli");

// Ensure the directory exists
if (!configDir.exists()) {
    configDir.mkdirs();
}
```
> Supported Store Types: CONF (config), CACHE (cache), LOG (logs), TEMP (temp files), VAR (app data), and RUN (runtime sockets/pids).

### Step 4.3: Unified Path Abstraction (Local & Remote)
NPath treats local files, HTTP URLs, SSH remote paths, classpath resources, and even internal files within remote Maven artifacts exactly the same way.

```
// 1. Local file
NPath localFile = NPath.of("/etc/myconfig.json");

// 2. Remote HTTP file
NPath remoteFile = NPath.of("https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/1.0.0/nuts-1.0.0.pom");

// 3. Remote SSH file (The DevOps Superpower!)
// Access a file on a remote server transparently. 
// No JSch, no manual stream management, no boilerplate!
NPath sshFile = NPath.of("ssh://admin@192.168.1.100/etc/nginx/nginx.conf");

// You can read it, write to it, or copy it just like a local file:
String remoteConfig = sshFile.readString();

// 4. Classpath resource (Bundled within your own application's JAR)
NPath classpathFile = NPath.of("classpath:/default-settings.json");

// 5. Resource from ANY Maven Artifact
NPath artifactResource = NPath.of("resource://net.thevpc.nuts:nuts#1.0.0/META-INF/MANIFEST.MF");
```

** The Consistency of the ssh:// Protocol:**
This isn't just limited to files. As we will see in Module 7, NAF's process execution engine (NExec) also uses the exact same ssh:// abstraction to run commands on remote servers:

### Step 4.4: Advanced Copying with Progress Monitoring (NCp)
To copy files—especially across protocols or large directories—NAF provides NCp. It handles stream management, directory recursion, and provides built-in progress monitoring.

```java
NPath source = NPath.of("https://example.com/large-diagnostic-tool.zip");
NPath target = NPath.ofUserStore(NStoreType.CACHE).resolve("tool.zip");

NCp.of()
    .from(source)
    .to(target)
    .progressMonitor(true) // Automatically prints a progress bar to the terminal!
    .run();
```
### Step 4.5: Data Integrity and Checksums (NDigest)
When downloading resources or moving critical configuration files, you must ensure data integrity. NAF provides NDigest to compute hashes (MD5, SHA-1, SHA-256, etc.) seamlessly.

```java
// Compute the SHA-256 hash of a local file
String sha256 = NDigest.of()
    .algorithm("SHA-256")
    .source(target) // Can be an NPath, InputStream, or byte[]
    .computeString();

NOut.println(NMsg.ofC("File checksum: ##%s##", sha256));
```

### Step 4.6: Implementing the check Command (Full Integration with Stream Validation)
Let's put it all together. We will update our `NutsAdminCLI.java` to implement the check command. This command will:

Locate the application's config and cache directories using `NStoreKey`.
Compute the expected `SHA-256` hash of a remote diagnostic definition file.
Download the file using `NCp`, validating the stream on the fly via `NCpValidator` and NAssert.
Print a styled report using `NOut` and `NTF`.

Update your `NutsAdminCLI.java` with the new check logic:

```java

package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {

    private static final NLog log = NLog.ofScoped(NutsAdminCLI.class);

    public static void main(String[] args) {
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().cmdLine();

        NRef<Boolean> verbose = NRef.of(false);
        NRef<String> configPath = NRef.ofNull();
        List<String> commands = new ArrayList<>();

        cmdLine.matcher()
            .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
            .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
            .withNonOption().matchAny(v -> commands.add(v.image()))
            .withDefaults()
            .requireAll();

        if (!cmdLine.isExecMode()) return;

        if (commands.isEmpty()) {
            NErr.println(NMsg.ofStyled("Error: Missing command. Use --help for usage.", NTextStyle.error()));
            return;
        }

        String command = commands.get(0);

        switch (command) {
            case "status":
                executeStatusCommand(verbose.get());
                break;
            case "check":
                executeCheckCommand(verbose.get());
                break;
            default:
                NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
        }
    }

    /**
     * Implements the 'check' command using NPath, NCp (with Validator), NDigest, and NAssert.
     */
    private void executeCheckCommand(boolean verbose) {
        log.info(NMsg.ofC("Executing 'check' command").withIntent(NLogIntent.START));
        
        NOut.println(NMsg.ofStyled("=== System Diagnostics Check ===", NTextStyle.primary1()));
        NOut.println();

        // 1. Resolve XDG-compliant directories using NStoreKey for the current user
        NPath configDir = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CONFIG, NApp.of().getId()));
        NPath cacheDir = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CACHE, NApp.of().getId()));
        
        if (!configDir.exists()) configDir.mkdirs();
        if (!cacheDir.exists()) cacheDir.mkdirs();

        NOut.println(NMsg.ofC("Config Dir: ##%s##", configDir));
        NOut.println(NMsg.ofC("Cache Dir:  ##%s##", cacheDir));
        NOut.println();

        // 2. Define a remote resource (using a small, stable Maven POM for demonstration)
        NPath remoteScript = NPath.of("https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/1.0.0/nuts-1.0.0.pom");
        NPath localScript = cacheDir.resolve("diagnostic-def.pom");

        // 3. Compute the expected hash from the remote source 
        // (In a real scenario, this might come from a trusted manifest or release notes)
        String expectedSha256 = NDigest.of()
            .setAlgorithm("SHA-256")
            .setSource(remoteScript)
            .computeString();

        NTrace.println("Fetching and verifying remote diagnostic definition...");
        
        // 4. Download the file using NCp with a built-in Stream Validator
        try {
            NCp.of()
                .from(remoteScript)
                .to(localScript)
                .setProgressMonitor(true) // Prints a nice progress bar
                .setOverwrite(true)
                .setValidator(new NCpValidator() {
                    @Override
                    public void validate(InputStream in) {
                        // Compute the hash of the incoming stream ON THE FLY
                        String actualSha256 = NDigest.of()
                            .setAlgorithm("SHA-256")
                            .setSource(in)
                            .computeString();
                        
                        // Assert that the downloaded stream matches the expected hash
                        // NAssert provides context-rich exceptions if the validation fails
                        NAssert.requireNamedEquals(
                            actualSha256, 
                            expectedSha256, 
                            "checksum",
                            () -> NMsg.ofC("Data integrity check failed for %s", localScript)
                        );
                    }
                })
                .run();
                
            NOut.println(NMsg.ofStyled(">> Download and integrity check complete.", NTextStyle.success()));
        } catch (Exception e) {
            NErr.println(NMsg.ofStyled(">> Failed to download or verify diagnostic definition!", NTextStyle.error()));
            log.error(NMsg.ofC("Download/Validation failed: %s", e.getMessage()).withIntent(NLogIntent.FAIL));
            return;
        }

        // 5. Print Final Report
        NOut.println();
        NOut.println(NMsg.ofC("Local File:   ##%s##", localScript));
        NOut.println(NMsg.ofC("File Size:    ##%d bytes##", localScript.length()));
        NOut.println(NMsg.ofC("SHA-256:      ##%s##", expectedSha256));
        NOut.println();
        
        NOut.println(NMsg.ofStyled("Diagnostics check completed successfully.", NTextStyle.success()));
        log.info(NMsg.ofPlain("Check command completed.").withIntent(NLogIntent.SUCCESS));
    }

    // ... (status command and lifecycle hooks remain the same as Module 3) ...
    private void executeStatusCommand(boolean verbose) {
        NOut.println(NMsg.ofStyled("=== System Status Report ===", NTextStyle.primary1()));
        NOut.println(NMsg.ofStyled("All systems operational.", NTextStyle.success()));
    }

    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofStyled(">> Initializing NutsAdminCLI workspace...", NTextStyle.primary1()));
    }

    @NAppUpdater
    public void onUpdateApplication() {
        NId appId = NApp.of().getId();
        NOut.println(NMsg.ofStyled(">> Updating NutsAdminCLI to " + appId.getVersion() + "...", NTextStyle.warn()));
    }

    @NAppUninstaller
    public void onUninstallApplication() {
        NOut.println(NMsg.ofStyled(">> Uninstalling NutsAdminCLI. Cleaning up resources...", NTextStyle.error()));
    }
}
```

## Module 5: Structured Data Parsing and Serialization
**Objective:** Read, manipulate, and write configuration files without relying on heavy external libraries like Jackson, Gson, or DOM4J. We will implement a new config command for our CLI that reads a JSON settings file, updates it in memory, saves it back to disk, and prints it to the terminal in a beautifully colorized format.
### Step 5.1: The Philosophy of NElement (Format-Agnostic Data Virtualization)
In the Java ecosystem, parsing JSON usually means pulling in Jackson or Gson. Parsing XML means pulling in DOM4J or JAXB. This violates NAF's zero-dependency philosophy and bloats your application.
NAF solves this with NElement, a powerful, format-agnostic, in-memory representation of structured data.

- Parse Once, Render Anywhere: You can parse a JSON file into an NElement, and then serialize that exact same object into XML, YAML, Properties, or TSON without changing your data model.
- Dynamic Tree Navigation: It acts as a dynamic tree (similar to JsonNode in Jackson), allowing you to traverse, modify, and build structured data on the fly using fluent builders.

### Step 5.2: Parsing and Creating Elements
To parse a file, we use NElementReader. To build data from scratch, we use the fluent builder API.

```java
// 1. Parse an existing JSON file from disk into an NElement
NPath settingsFile = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CONFIG, NApp.of().getId()))
                          .resolve("settings.json");

// If the file doesn't exist, let's create a default one
if (!settingsFile.exists()) {
    NElement defaultConfig = NElement.ofObjectBuilder()
        .set("diagnostics-level", "INFO")
        .set("auto-update", true)
        .set("last-run", "never")
        .build();
    
    // Save it immediately as JSON
    NElementWriter.ofPlainJson(defaultConfig).println(settingsFile);
}

// Now parse it back into memory
NElement config = NElementReader.ofJson().read(settingsFile);
```

### Step 5.3: Navigating and Manipulating the Data Tree
Once the data is in memory as an NElement, you can navigate it dynamically. Because we prefer immutable builders where possible, we can extract values, modify them, and build a new state.

```java
// Read a specific value safely
String level = config.getStringValue("diagnostics-level").orElse("INFO");

// Update the "last-run" timestamp to the current time
// NElement objects can be manipulated directly or rebuilt.
// Here we update the existing tree in memory:
config.set("last-run", java.time.Instant.now());

// Add a new nested object dynamically
config.set("network", NElement.ofObjectBuilder()
    .set("timeout-ms", 5000)
    .set("retries", 3)
    .build());
```

### Step 5.4: Multi-Format Serialization (The Conversion Superpower)

This is where NElement truly shines. Because the data is virtualized, converting between formats is a one-liner.
Let's say we want to save the configuration back to disk as JSON, but we also want to print it to the user's terminal. For the terminal, we use TSON (Nuts Type Safe Object Notation), which renders the JSON structure with beautiful syntax highlighting and colors using NTF.

```java
// 1. READING: Parse an existing JSON file into an NElement
NPath settingsFile = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CONFIG, NApp.of().getId()))
                          .resolve("settings.json");

// Use NElementReader to read the file
NElement config = NElementReader.ofJson().read(settingsFile);

// (Or if it was saved as TSON, we use the TSON reader!)
// NElement config = NElementReader.ofTson().read(settingsFile);


// 2. WRITING TO DISK: Save the updated configuration as pure JSON (No NTF markup)
// This ensures the file on disk is valid, parseable JSON.
NElementWriter.ofPlainJson(config).println(settingsFile);


// 3. WRITING TO TERMINAL: Print the configuration to the console with syntax coloring
// This uses the NTF-enabled writer to colorize keys, strings, and numbers in the terminal.
NOut.println(NMsg.ofStyled("Current Active Configuration:", NTextStyle.primary2()));
NElementWriter.ofNtfJson(config).println(); 
// (Or if we prefer to display it as colorized TSON in the terminal):
// NElementWriter.ofNtfTson(config).println();
```

### Step 5.5: Implementing the config Command (Full Integration)

Let's put it all together. We will add a config command to our NutsAdminCLI. This command will:

- Locate the settings.json file in the user's XDG config directory.
- Load it into an NElement.
- Update the last-run timestamp.
- Save it back to disk.
- Print the final configuration to the terminal using beautiful TSON formatting.

Update your `NutsAdminCLI.java` to include the new config logic:

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {

    private static final NLog log = NLog.ofScoped(NutsAdminCLI.class);

    public static void main(String[] args) {
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().cmdLine();

        NRef<Boolean> verbose = NRef.of(false);
        NRef<String> configPath = NRef.ofNull();
        List<String> commands = new ArrayList<>();

        cmdLine.matcher()
            .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
            .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
            .withNonOption().matchAny(v -> commands.add(v.image()))
            .withDefaults()
            .requireAll();

        if (!cmdLine.isExecMode()) return;

        if (commands.isEmpty()) {
            NErr.println(NMsg.ofStyled("Error: Missing command. Use --help for usage.", NTextStyle.error()));
            return;
        }

        String command = commands.get(0);

        switch (command) {
            case "status":
                executeStatusCommand(verbose.get());
                break;
            case "check":
                executeCheckCommand(verbose.get());
                break;
            case "config":
                executeConfigCommand();
                break;
            default:
                NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
        }
    }

    /**
     * Implements the 'config' command using NElement, NElementReader, and NElementWriter.
     */
    private void executeConfigCommand() {
        log.info(NMsg.ofC("Executing 'config' command").withIntent(NLogIntent.READ));
        
        NOut.println(NMsg.ofStyled("=== Configuration Manager ===", NTextStyle.primary1()));
        NOut.println();

        // 1. Resolve the settings file path using NStoreKey
        NPath configDir = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CONFIG, NApp.of().getId()));
        NPath settingsFile = configDir.resolve("settings.json");

        if (!configDir.exists()) configDir.mkdirs();

        // 2. Initialize default config if it doesn't exist
        NElement config;
        if (!settingsFile.exists()) {
            NTrace.println("No settings found. Generating defaults...");
            config = NElement.ofObjectBuilder()
                .set("diagnostics-level", "INFO")
                .set("auto-update", true)
                .set("last-run", "never")
                .build();
            
            // Save defaults to disk as JSON
            NElementWriter.ofPlainJson(config).println(settingsFile);
            NOut.println(NMsg.ofStyled(">> Created default settings.json", NTextStyle.success()));
        } else {
            // 3. Parse existing JSON into NElement
            NTrace.println("Loading existing settings...");
            config = NElementReader.ofJson().read(settingsFile);
        }

        // 4. Manipulate the data in memory
        config.set("last-run", Instant.now().toString());
        
        // Add a nested object dynamically
        config.set("network", NElement.ofObjectBuilder()
            .set("timeout-ms", 5000)
            .set("retries", 3)
            .build());

        // 5. Save the updated configuration back to disk as JSON
        NElementWriter.ofPlainJson(config).println(settingsFile);
        NOut.println(NMsg.ofStyled(">> Settings updated and saved.", NTextStyle.success()));
        NOut.println();

        // 6. Print the configuration to the terminal using NTF-colored TSON
        NOut.println(NMsg.ofStyled("Current Active Configuration:", NTextStyle.primary2()));
        NElementWriter.ofNtfTson(config).println();
        
        log.info(NMsg.ofPlain("Config command completed.").withIntent(NLogIntent.SUCCESS));
    }

    // ... (status, check commands and lifecycle hooks remain the same) ...
    private void executeStatusCommand(boolean verbose) {
        NOut.println(NMsg.ofStyled("=== System Status Report ===", NTextStyle.primary1()));
        NOut.println(NMsg.ofStyled("All systems operational.", NTextStyle.success()));
    }

    private void executeCheckCommand(boolean verbose) {
        NOut.println(NMsg.ofStyled("=== System Diagnostics Check ===", NTextStyle.primary1()));
        NOut.println(NMsg.ofStyled("Diagnostics complete.", NTextStyle.success()));
    }

    @NAppInstall
    public void onInstallApplication() {
        NOut.println(NMsg.ofStyled(">> Initializing NutsAdminCLI workspace...", NTextStyle.primary1()));
    }

    @NAppUpdater
    public void onUpdateApplication() {
        NId appId = NApp.of().getId();
        NOut.println(NMsg.ofStyled(">> Updating NutsAdminCLI to " + appId.getVersion() + "...", NTextStyle.warn()));
    }

    @NAppUninstaller
    public void onUninstallApplication() {
        NOut.println(NMsg.ofStyled(">> Uninstalling NutsAdminCLI. Cleaning up resources...", NTextStyle.error()));
    }
}
```

### Step 5.6: Testing the New Features

Rebuild (change version) and reinstall your application:

```bash
mvn clean install
nuts install com.mycompany.admin:nuts-admin-cli
```

Now, let's test the config command:

1. First Run (Generates and Prints Defaults):

```bash
nuts nuts-admin-cli config
```

Output:

```
=== Configuration Manager ===

>> Created default settings.json
>> Settings updated and saved.

Current Active Configuration:
{
  "diagnostics-level": "INFO",
  "auto-update": true,
  "last-run": "2026-06-18T10:00:00Z",
  "network": {
    "timeout-ms": 5000,
    "retries": 3
  }
}
```

> (Note: In your actual terminal, the keys, strings, booleans, and numbers will be beautifully colorized using NTF/TSON syntax highlighting!)

2. Subsequent Runs (Updates Timestamp):
If you run it again, it will load the existing file, update the last-run timestamp, save it, and print the updated tree.

3. Trace Mode:

```bash
nuts nuts-admin-cli --trace config
```

### Summary of Module 5
We have successfully eliminated the need for heavy JSON/XML libraries.

- We used NElementReader to seamlessly parse JSON from disk into memory.
- We used NElement.ofObjectBuilder() to construct and manipulate structured data dynamically.
- We used NElementWriter to serialize the data back to disk as standard JSON, and simultaneously render it to the terminal as colorized TSON.
- We integrated this flawlessly with the NStoreKey XDG directory resolution we built in Module 4.

## Module 6: Concurrency, Resilience Patterns, and Error Modeling

**Objective:** Implement enterprise-grade fault tolerance, load distribution, and concurrency controls. We will build a sync command for our CLI that safely prevents concurrent executions, intelligently distributes diagnostic checks across multiple workers, throttles API calls, resiliently caches remote configurations, and executes multi-step workflows with automatic rollback capabilities.

### Step 6.1: Tri-State Error Modeling with NOptional
In standard Java, java.util.Optional forces you to treat a technical failure (e.g., IOException) the same as a logically missing value (e.g., "User has no config"). This leads to silent failures and lost context.
NAF introduces NOptional, a powerful tri-state alternative that explicitly models three distinct states:

- PRESENT: The value exists.
- EMPTY: The value is logically missing.
- ERROR: A technical failure occurred during evaluation.

```java
public NOptional<String> fetchRemoteConfig() {
    try {
        String data = downloadConfig();
        return NOptional.of(data); // PRESENT
    } catch (FileNotFoundException e) {
        return NOptional.ofNamedEmpty("remote-config"); // EMPTY
    } catch (IOException e) {
        return NOptional.ofError(e); // ERROR
    }
}

// Usage:
NOptional<String> config = fetchRemoteConfig();

if (config.isPresent()) {
    NOut.println("Config loaded: " + config.get());
} else if (config.isEmpty()) {
    NOut.println("No configuration found for this user.");
} else if (config.isError()) {
    // Explicitly handle the technical failure without a try/catch block!
    NErr.println(NMsg.ofC("Failed to load config: %s", config.getError()));
}

// Fluent chaining with safe navigation (similar to ?. in Kotlin/TypeScript)
String timeout = NOptional.of(appConfig)
    .then(c -> c.network)
    .then(n -> n.timeout)
    .orElse(5000); // Fallback if any part of the chain is null/empty
```
### Step 6.2: Automatic Retries with NRetryCall
To protect unreliable network operations, NAF provides NRetryCall. It allows you to wrap a flaky operation and define a deterministic retry strategy with exponential backoff and custom recovery actions.

```java
NRetryCall<String> retryCall = NRetryCall.of(() -> downloadManifest())
    .setMaxRetries(5)
    .setExponentialRetryPeriod(Duration.ofSeconds(1), 2.0) // Waits: 1s, 2s, 4s, 8s...
    .setRecover(() -> "fallback-manifest-v1"); // Fallback if all retries fail

String manifest = retryCall.call();
```

### Step 6.3: Throttling and API Protection with NRateLimitValue

When interacting with external diagnostic APIs or local system resources, you must prevent overuse. NRateLimitValue provides thread-safe, strategy-based rate limiting (e.g., sliding window).

```java
NRateLimitedValue apiLimiter = NRateLimitedValue.ofBuilder("diagnostics-api")
    .withLimit("calls", 10).per(Duration.ofMinutes(1))
    .withStrategy(NRateLimitDefaultStrategy.SLIDING_WINDOW)
    .build();

// claimAndRun blocks until a permit is available, then executes the task
apiLimiter.claimAndRun(() -> {
    NOut.println("Executing rate-limited diagnostic check...");
});
```

### Step 6.4: Intelligent Load Distribution with NWorkBalancer
When running multiple diagnostic checks, you can distribute the workload across multiple logical workers using NWorkBalancer. It supports weighted distribution and strategies like Round-Robin or Least-Load.

```java
NWorkBalancer<String> balancer = NWorkBalancerFactory.of().<String>ofBuilder("diagnostics")
    .addWorker("Worker-1").withWeight(1)
    .addWorker("Worker-2").withWeight(2) // Worker-2 gets twice the load
    .setStrategy(NWorkBalancerDefaultStrategy.LEAST_LOAD)
    .build();

// Wrap the job in an NCallable that tracks metrics and selects a worker
NCallable<String> callable = balancer.of("check-cpu", context -> {
    NOut.println(NMsg.ofC("Running on %s", context.getWorkerName()));
    return "CPU OK";
});

// Execute the balanced tasks concurrently using NTaskSet
NTaskSet tasks = NTaskSet.of();
for (int i = 0; i < 10; i++) {
    tasks.call(callable);
}
tasks.join(); // Wait for all tasks to complete

```

### Step 6.5: Resilient Caching with NCachedValue

`NCachedValue` is designed for expensive computations or network fetches. Its superpower is fallback retention: if the Time-To-Live (TTL) expires and the refresh call fails, NCachedValue will automatically retain and return the last known good value instead of crashing.

```java
NCachedValue<String> cachedManifest = NCachedValue.of(() -> downloadManifest())
    .setExpiry(Duration.ofMinutes(5))
    .setRetry(3, Duration.ofMillis(100))   // Retry up to 3 times on failure
    .retainLastOnFailure(true);            // Keep the app running with stale data if network drops

String data = cachedManifest.get(); 
```

### Step 6.6: Compensating Transactions with NSaga
System administration often involves multi-step workflows (e.g., Download -> Validate -> Apply). If the "Apply" step fails, you must "undo" the previous steps to leave the system in a clean state.
NAF provides NSagaCallable to define these compensating transactions cleanly:

```java
NSagaCallable<Object> saga = NConcurrent.of().sagaCallBuilder()
    .start()
    .then("Download Assets", new DownloadStep())
    .then("Validate Assets", new ValidateStep())
    .then("Apply Config", new ApplyStep())
    .end().build();

// If 'Apply Config' fails, 'Validate' and 'Download' compensations run automatically!
saga.call(); 
```

### Step 6.7: Inter-Process Locking with NLock
To prevent a user from accidentally running nuts-admin-cli sync in two different terminal windows simultaneously (which could corrupt system files), we use NLock. It seamlessly handles both intra-process (memory) and inter-process (file-based) locking.

```java
NPath lockFile = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.RUN, NApp.of().getId()))
                      .resolve("sync.lock");

NLock lock = NLock.ofPath(lockFile);

// runWith ensures the block is executed by only ONE process across the entire OS
lock.runWith(() -> {
    NOut.println("Lock acquired. Running critical sync operations...");
});
```

### Step 6.8: Implementing the sync Command (Full Integration)
Let's combine these resilience patterns into a new sync command. This command will acquire an OS-level lock, fetch a remote manifest using resilient caching, distribute diagnostic checks across balanced workers, and execute a multi-step synchronization saga.
Update your NutsAdminCLI.java to include the sync logic:

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {

    private static final NLog log = NLog.ofScoped(NutsAdminCLI.class);

    // Resilient cache for the remote manifest
    private static final NCachedValue<String> manifestCache = NCachedValue.of(() -> fetchRemoteManifest())
        .setExpiry(Duration.ofMinutes(5))
        .setRetry(3, Duration.ofMillis(100))
        .retainLastOnFailure(true);

    // Rate limiter for diagnostic API calls
    private static final NRateLimitedValue apiLimiter = NRateLimitedValue.ofBuilder("diagnostics-api")
        .withLimit("calls", 20).per(Duration.ofMinutes(1))
        .withStrategy(NRateLimitDefaultStrategy.SLIDING_WINDOW)
        .build();

    // Work balancer for concurrent checks
    private static final NWorkBalancer<String> balancer = NWorkBalancerFactory.of().<String>ofBuilder("diagnostics")
        .addWorker("Worker-1").withWeight(1)
        .addWorker("Worker-2").withWeight(2)
        .setStrategy(NWorkBalancerDefaultStrategy.LEAST_LOAD)
        .build();

    public static void main(String[] args) {
        new NutsAdminCLI().run(NAppRunOptions.ofExit(args));
    }

    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().cmdLine();

        NRef<Boolean> verbose = NRef.of(false);
        NRef<String> configPath = NRef.ofNull();
        List<String> commands = new ArrayList<>();

        cmdLine.matcher()
            .with("-v", "--verbose").matchFlag(v -> verbose.set(v.booleanValue()))
            .with("-c", "--config").matchEntry(v -> configPath.set(v.stringValue()))
            .withNonOption().matchAny(v -> commands.add(v.image()))
            .withDefaults()
            .requireAll();

        if (!cmdLine.isExecMode()) return;

        if (commands.isEmpty()) {
            NErr.println(NMsg.ofStyled("Error: Missing command. Use --help for usage.", NTextStyle.error()));
            return;
        }

        String command = commands.get(0);

        switch (command) {
            case "status": executeStatusCommand(verbose.get()); break;
            case "check": executeCheckCommand(verbose.get()); break;
            case "config": executeConfigCommand(); break;
            case "sync": executeSyncCommand(); break;
            default: NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
        }
    }

    /**
     * Implements the 'sync' command using NLock, NRetryCall, NCachedValue, NRateLimitValue, NWorkBalancer, and NSaga.
     */
    private void executeSyncCommand() {
        log.info(NMsg.ofPlain("Initiating synchronization workflow.").withIntent(NLogIntent.START));

        // 1. Acquire an inter-process lock
        NPath lockFile = NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.RUN, NApp.of().getId()))
                              .resolve("sync.lock");
        NLock lock = NLock.ofPath(lockFile);

        try {
            lock.runWith(() -> {
                NOut.println(NMsg.ofStyled(">> Lock acquired. Starting sync...", NTextStyle.primary1()));

                // 2. Fetch manifest using Resilient Cache
                NOut.println("Fetching remote manifest...");
                String manifest = manifestCache.get(); 
                NTrace.println(NMsg.ofC("Manifest loaded: %s", manifest));

                // 3. Execute Diagnostic Checks using Load Balancer and Rate Limiter
                NOut.println("Executing balanced diagnostic checks...");
                NTaskSet tasks = NTaskSet.of();
                
                String[] targets = {"cpu", "memory", "disk", "network"};
                for (String target : targets) {
                    NCallable<String> callable = balancer.of(target, context -> {
                        // Rate limit the execution
                        apiLimiter.claimAndRun(() -> {
                            NTrace.println(NMsg.ofC("Checking %s on %s", target, context.getWorkerName()));
                            simulateDelay(200);
                        });
                        return "Completed " + target;
                    });
                    tasks.call(callable);
                }
                tasks.join(); // Wait for all balanced tasks

                // 4. Execute Multi-Step Saga with Compensating Transactions
                NOut.println("Executing synchronization saga...");
                NConcurrent.of().sagaCallBuilder()
                    .start()
                    .then("Download Assets", new SimulatedStep("Download"))
                    .then("Validate Assets", new SimulatedStep("Validate"))
                    .then("Apply Configuration", new SimulatedStep("Apply"))
                    .end().build()
                    .call();

                NOut.println(NMsg.ofStyled(">> Synchronization completed successfully.", NTextStyle.success()));
                log.info(NMsg.ofPlain("Sync workflow finished.").withIntent(NLogIntent.SUCCESS));
            });
        } catch (Exception e) {
            NErr.println(NMsg.ofStyled("Error: Synchronization failed or lock unavailable.", NTextStyle.error()));
            log.error(NMsg.ofC("Sync failure: %s", e.getMessage()).withIntent(NLogIntent.FAIL));
        }
    }

    // --- Helper Classes and Methods ---

    private static String fetchRemoteManifest() {
        return NRetryCall.of(() -> {
            NTrace.println("Attempting network fetch...");
            return "manifest-v2.4-" + System.currentTimeMillis();
        })
        .setMaxRetries(3)
        .setExponentialRetryPeriod(Duration.ofMillis(500), 2.0)
        .call();
    }

    private void simulateDelay(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // Simulated Saga Step
    private static class SimulatedStep implements NSagaStep {
        private final String name;
        public SimulatedStep(String name) { this.name = name; }

        @Override
        public Object call(NSagaContext context) {
            NTrace.println(NMsg.ofC("Executing step: %s", name));
            simulateDelay(100);
            // Uncomment to test Saga rollback:
            // if ("Apply".equals(name)) throw new RuntimeException("Disk Full!");
            return name;
        }

        @Override
        public void undo(NSagaContext context) {
            NErr.println(NMsg.ofStyled("[ROLLBACK] Undoing step: " + name, NTextStyle.warn()));
        }
    }

    // ... (Other commands and lifecycle hooks remain the same) ...
    private void executeStatusCommand(boolean verbose) { /* ... */ }
    private void executeCheckCommand(boolean verbose) { /* ... */ }
    private void executeConfigCommand() { /* ... */ }

    @NAppInstall public void onInstallApplication() { /* ... */ }
    @NAppUpdater public void onUpdateApplication() { /* ... */ }
    @NAppUninstaller public void onUninstallApplication() { /* ... */ }
}
```

### Step 6.9: Smart Collection Synchronization with NCollectionDiff

We will enhance the sync command in Module 6. Instead of just downloading a file, let's simulate synchronizing a list of Monitored Services (e.g., endpoints our CLI checks for uptime) from a remote configuration to our local database.
This is where NCollectionDiff shines, especially if you are using JPA/Hibernate. It prevents the destructive "clear and re-add" anti-pattern.
Update the executeSyncCommand method (add this after the Saga execution):

```java
// --- Simulating JPA/ORM Collection Synchronization ---
NOut.println();
NOut.println(NMsg.ofStyled("=== Synchronizing Monitored Services (JPA Style) ===", NTextStyle.primary1()));

// Simulate "Old" services currently in the local database
List<MonitoredService> dbServices = Arrays.asList(
    new MonitoredService("svc-nginx", "http://localhost:80", 60),
    new MonitoredService("svc-postgres", "jdbc:localhost:5432", 30), // Will be CHANGED
    new MonitoredService("svc-redis", "redis://localhost:6379", 30)   // Will be REMOVED
);

// Simulate "New" services fetched from remote configuration
List<MonitoredService> remoteServices = Arrays.asList(
    new MonitoredService("svc-nginx", "http://localhost:80", 60),       // UNCHANGED
    new MonitoredService("svc-postgres", "jdbc:localhost:5433", 30),    // CHANGED (port updated)
    new MonitoredService("svc-memcached", "memcached://localhost:11211", 45) // ADDED
);

NOut.println(NMsg.ofC("Comparing %d local services with %d remote services...", dbServices.size(), remoteServices.size()));

// Use NCollectionDiff to compute the delta based on the Service's ID
for (NCollectionDiffChange<MonitoredService> diff : NCollectionDiff
        .of(dbServices, remoteServices)
        .idResolver(e -> e.id())
        .equalizer((a, b) ->
        a.getName().equals(b.getName()) &&
        a.getEndpoint().equals(b.getEndpoint())
        // Ignore updatedAt, createdAt, etc.
        ).diff()
        ) {
    switch (diff.mode()) {
        case ADDED: {
            NOut.println(NMsg.ofStyled("[INSERT] ", NTextStyle.success()).append(NMsg.ofC("Adding new service: %s", diff.newValue().id())));
            // JPA equivalent: entityManager.persist(diff.newValue());
            break;
        }
        case REMOVED: {
            NOut.println(NMsg.ofStyled("[DELETE] ", NTextStyle.error()).append(NMsg.ofC("Removing obsolete service: %s", diff.oldValue().id())));
            // JPA equivalent: entityManager.remove(entityManager.merge(diff.oldValue()));
            break;
        }
        case CHANGED: {
            NOut.println(NMsg.ofStyled("[UPDATE] ", NTextStyle.warn()).append(NMsg.ofC("Updating service: %s (Preserving ID and created_at!)", diff.newValue().id())));
            // JPA equivalent: Update fields of the managed entity (diff.oldValue()) 
            // with values from diff.newValue(). This avoids deleting and re-inserting!
            break;
        }
        case UNCHANGED: {
            // Do nothing. The entity remains untouched in the database.
            break;
        }
    }
}
private record MonitoredService(String id, String endpoint, int checkIntervalSec) {}
```

### Step 6.10: Testing Resilience and Rollbacks

Rebuild and test the new sync command:

1. Successful Sync:

```bash
nuts nuts-admin-cli --trace sync
```
Output:
```
>> Lock acquired. Starting sync...
Fetching remote manifest...
Attempting network fetch...
Executing balanced diagnostic checks...
Checking cpu on Worker-2
Checking memory on Worker-1
...
Executing synchronization saga...
Executing step: Download
Executing step: Validate
Executing step: Apply
>> Synchronization completed successfully.
```

2. Testing the Saga Rollback:

If you uncomment the throw new RuntimeException("Disk Full!"); inside the Apply step and run the command again, the Saga engine will catch the failure and automatically trigger the compensation logic for the previous steps:

Output:
```
Executing step: Apply
[ROLLBACK] Undoing step: Validate
[ROLLBACK] Undoing step: Download
Error: Synchronization failed or lock unavailable.
```

3. Testing the Inter-Process Lock:

Open two terminal windows. Run nuts nuts-admin-cli sync in the first one (add a Thread.sleep(5000) in simulateDelay to slow it down). Immediately run it in the second window.
Output in Window 2:

```
Error: Synchronization failed or lock unavailable.
```

### Summary of Module 6
We have transformed our CLI from a simple script into an enterprise-grade, resilient tool.

- We used `NOptional` to safely model technical errors vs. missing data.
- We used `NRetryCall` to handle transient network failures with exponential backoff.
- We used `NRateLimitValue` to protect external APIs from being overwhelmed.
- We used `NWorkBalancer` to distribute diagnostic checks across weighted workers.
- We used `NCachedValue` to ensure the application remains operational using stale data if the network drops.
- We used `NSaga` to guarantee system consistency by automatically rolling back partial workflows upon failure.
- We used `NLock` to guarantee OS-level mutual exclusion.

## Module 7: External Process Execution and System Monitoring
**Objective:** Execute native OS commands safely, capture their output without deadlocks, run commands on remote servers via SSH, and monitor running system processes. We will implement an inspect command for our CLI that gathers local system information, discovers running Java processes, and displays them in a beautifully formatted ASCII table using NTextArt.
### Step 7.1: The Limitations of Standard Java Process APIs
In standard Java, executing external commands requires Runtime.exec() or ProcessBuilder. While functional, they are notorious for causing developer headaches:

- Stream Deadlocks: If you don't manually consume stdout and stderr on separate threads, the OS buffer fills up and the child process hangs indefinitely.
- Boilerplate: Reading streams, waiting for exit codes, and handling exceptions requires dozens of lines of repetitive code.
- No Native Remote Execution: Running a command on a remote server requires pulling in SSH libraries and managing sessions manually.

`NAF` solves all of this with `NExec` and `NPs`, providing a unified, deadlock-free, and cross-platform execution engine.
### Step 7.2: Safe and Fluent Local Execution with NExec
`NExec` automatically handles stream consumption, environment scrubbing, and cross-platform quirks. You simply define the command, tell it what to capture, and execute.
```java
// Execute a local command and capture stdout
String hostname = NExec.of()
    .command("hostname")
    .failFast()         // Throws an exception immediately if the exit code is non-zero
    .grabbedOut()
    .trim();

NOut.println(NMsg.ofC("Running on host: ##%s##", hostname));
```

### Step 7.3: The DevOps Superpower: Remote Execution via SSH
Because NExec is deeply integrated with NAF's protocol abstraction, you can execute commands on remote servers simply by changing the target. No JSch, no manual SSH session management—just a fluent .at() call.

```java
// Execute a command on a remote server via SSH
String remoteUptime = NExec.of()
    .at("ssh://admin@192.168.1.100") // Transparently handles SSH connection
    .command("uptime")
    .grabbedOut();

NOut.println(NMsg.ofC("Remote server uptime: ##%s##", remoteUptime));
```
> (Note: As discussed in Module 4, the SSH extension is dynamically loaded by Nuts the first time you use the ssh:// protocol, keeping your core JAR lightweight!)

### Step 7.4: Process Discovery and Management with NPs

To monitor the system, NAF provides NPs, a cross-platform API for discovering, filtering, and terminating running processes. It abstracts away the differences between Linux (ps), Windows (tasklist), and macOS.

```java
// Discover all running Java processes
List<NPsInfo> javaProcesses = NPs.of()
    .setPlatformFamily(NPlatformFamily.JAVA)
    .getResultList();

for (NPsInfo ps : javaProcesses) {
    NOut.println(NMsg.ofC("PID: ##%s## | Command: ##%s##", ps.getPid(), ps.getName()));
}

// Gracefully terminate a specific process if needed
if (NPs.of().isSupportedKillProcess()) {
    NPs.of().killProcess(12345); 
}
```

### Step 7.5: Implementing the inspect Command with NTextArt Tables

Let's put it all together. We will add an inspect command to our NutsAdminCLI. This command will:

- Use NExec to fetch the local hostname.
- Use NPs to discover all running Java processes.
- Use NTextArt to render the process list as a beautiful, structured ASCII table.

First, update your `run()` method's switch statement to route the inspect command:

```java

NTextArt art = NTextArt.of();
// 2. Define the text to render (can be styled with NTF!)
NText text = NText.ofStyled("NutsAdmin", NTextStyle.primary1());
// 3. Render using the "figlet:standard" font
// NTextArt provides multiple built-in fonts (e.g., "figlet:block", "figlet:slant")
NText renderedBanner = art.getTextRenderer("figlet:standard").get().render(text);
// 4. Print to the terminal
NOut.println(renderedBanner);
 

switch (command) {
    case "status": executeStatusCommand(verbose.get()); break;
    case "check": executeCheckCommand(verbose.get()); break;
    case "config": executeConfigCommand(); break;
    case "sync": executeSyncCommand(); break;
    case "inspect": executeInspectCommand(); break; // NEW
    default: NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
}
```

Now, implement the executeInspectCommand method:

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.util.List;
// ... other imports

@NAppDefinition
public class NutsAdminCLI {
    // ... (previous code remains the same)

    /**
     * Implements the 'inspect' command using NExec, NPs, and NTextArt.
     */
    private void executeInspectCommand() {
        log.info(NMsg.ofPlain("Executing 'inspect' command").withIntent(NLogIntent.START));
        
        NOut.println(NMsg.ofStyled("=== System & Process Inspector ===", NTextStyle.primary1()));
        NOut.println();

        // 1. Fetch Local System Info using NExec
        NTrace.println("Fetching local system information...");
        String hostname = "Unknown";
        try {
            hostname = NExec.of()
                .command("hostname")
                .grabbedOut()
                .trim();
        } catch (Exception e) {
            NTrace.println(NMsg.ofC("Could not fetch hostname: %s", e.getMessage()));
        }
        
        NOut.println(NMsg.ofC("Local Hostname: ##%s##", hostname));
        NOut.println();

        // 2. Process Discovery using NPs
        NTrace.println("Discovering running Java processes...");
        List<NPsInfo> processes = NPs.of()
            .setPlatformFamily(NPlatformFamily.JAVA)
            .getResultList();

        if (processes.isEmpty()) {
            NOut.println(NMsg.ofStyled("No Java processes found.", NTextStyle.warn()));
        } else {
            NOut.println(NMsg.ofC("Found ##%d## Java processes:", processes.size()));
            NOut.println();
            
            // 3. Render as a beautiful ASCII Table using NTextArt
            NMutableTableModel table = NTableModel.of()
                .addHeaderRow(
                    NText.ofStyled("PID", NTextStyle.primary1()),
                    NText.ofStyled("Name / Command", NTextStyle.primary1()),
                    NText.ofStyled("Status", NTextStyle.primary1())
                );

            for (NPsInfo ps : processes) {
                // Truncate long command names for better table formatting
                String cmdName = ps.getName();
                if (cmdName.length() > 50) {
                    cmdName = cmdName.substring(0, 47) + "...";
                }

                table.addRow(
                    NText.of(String.valueOf(ps.getPid())),
                    NText.of(cmdName),
                    NText.ofStyled("Running", NTextStyle.success())
                );
            }

            // Render and print the table
            NOut.println(NTextArt.of().tableRenderer().get().render(table));
        }
        
        NOut.println();
        NOut.println(NMsg.ofStyled("Inspection complete.", NTextStyle.success()));
        log.info(NMsg.ofPlain("Inspect command completed").withIntent(NLogIntent.SUCCESS));
    }
    
    // ... (other commands and lifecycle hooks)
}
```

### Step 7.6: Testing the New Features

Change version, Rebuild and update your application:
mvn clean install
nuts update com.mycompany.admin:nuts-admin-cli

1. Normal Execution:
```bash
nuts nuts-admin-cli inspect
```

Output:

```
=== System & Process Inspector ===

Local Hostname: my-dev-machine

Found 3 Java processes:

+-------+----------------------------------------------------+---------+
| PID   | Name / Command                                     | Status  |
+-------+----------------------------------------------------+---------+
| 1452  | /usr/lib/jvm/java-11/bin/java -jar nuts-admin...   | Running |
| 8831  | org.apache.catalina.startup.Bootstrap start        | Running |
| 9102  | net.thevpc.nuts.boot.NutsWorkspaceMain             | Running |
+-------+----------------------------------------------------+---------+

Inspection complete.
```

> (Notice how NTextArt automatically calculates column widths, draws the ASCII borders, and applies the NTF colors to the headers and status!)

Summary of Module 7
We have successfully integrated external process execution and system monitoring into our CLI.

- We used `NExec` to safely run local commands and capture output without stream deadlocks.
- We highlighted the `ssh://` protocol integration for seamless remote execution.
- We used `NPs` to discover running Java processes across different operating systems.
- We leveraged `NTextArt` and `NMutableTableModel` to render structured data as beautiful, colorized ASCII tables, elevating the CLI's visual output to a professional level.


## Module 8: Runtime Expression Evaluation
**Objective:** Implement dynamic, runtime expression parsing and evaluation using NAF's advanced Context-based NExpr engine. We will add an eval command to our CLI that allows users to pass mathematical, logical, or custom expressions, inject runtime system metrics as variables, register custom functions, and even define custom operators on the fly.

### Step 8.1: The Power of the Context-Based NExpr API

Unlike basic expression evaluators that only support simple math, NAF's NExpr is a fully-fledged, context-aware evaluation engine.

- C/Java Syntax by Default: It natively supports standard C/Java operators (+, -, *, /, >, <, &&, ||). Note: Text-based operators like AND or OR are not supported by default; you must use && and ||.
- Rich Built-ins: It comes with out-of-the-box support for Math constants/functions, Physics constants, and standard built-in utilities.
- Extensibility: You can dynamically inject variables, custom functions, and even custom operators into the evaluation context.

### Step 8.2: Building the Evaluation Context
To evaluate an expression, you build an NExprContext using the fluent NExprContextBuilder. The builder allows you to define the entire "vocabulary" of your expression—built-ins, custom constants, custom functions, custom operators, and dynamic variables—in a single, readable chain.
The builder offers two terminal methods:

- build(): Creates an immutable context. This is thread-safe, highly performant, and perfect for contexts that are defined once and reused across multiple evaluations.
- buildMutable(): Creates a mutable context. Use this only if you need to dynamically add, remove, or change declarations at runtime (e.g., loading plugins or user-defined functions on the fly).

```java
// 1. Define the ENTIRE context in a fluent, immutable builder
NExprContext ctx = NExprContextBuilder.of()
    // Rich built-ins
    .declareBuiltins()
    .declareMathConstants()
    .declareMathFunctions()
    .declarePhysicsConstants()
    
    // Custom Constants
    .declareConstant("max_threshold", 0.95)
    
    // Custom Functions (e.g., 'either' returns the first non-blank argument)
    .declareFunction(new EitherFct())
    
    // Custom Operators (e.g., '->' to generate an array range)
    .declareOperator(NExprOperator.of(
        "->", 
        NExprOpType.INFIX, 
        NExprOpPrecedence.ASSIGN + 1, 
        NOperatorAssociativity.LEFT, 
        opCtx -> {
            int o1 = NLiteral.of(opCtx.arg(0).get().eval(opCtx)).asInt().orElse(0);
            int o2 = NLiteral.of(opCtx.arg(1).get().eval(opCtx)).asInt().orElse(o1);
            return NArrays.range(o1, o2);
        }
    ))
    
    // Dynamic Variable Resolution (Runtime metrics)
    .declareVars(varName -> {
        switch (varName) {
            case "cpu_load": return NOptional.of(NExprNodeValue.of(random.nextDouble()));
            case "mem_free": return NOptional.of(NExprNodeValue.of(random.nextInt(4000) + 1000));
            default:         return NOptional.ofNamedEmpty("var " + varName);
        }
    })
    .build(); // <-- Creates an IMMUTABLE, thread-safe context
```

### Step 8.3: When to use buildMutable()

In enterprise applications, you often have a "global" set of functions and constants (Math, Physics, custom utilities) that are expensive to build. You want to build this Base Context once, make it immutable and thread-safe, and reuse it across the entire application.
However, when evaluating a specific expression, you might need to inject a few local variables or override a specific function for that scope. This is where childContext() shines. It allows you to create a lightweight, inherited context without mutating the parent.

```java
// 1. Build a heavy, immutable base context (e.g., at application startup)
// This is thread-safe and can be shared across all threads.
NExprContext baseCtx = NExprContextBuilder.of()
                .declareBuiltins()
                .declareMathConstants()
                .declareMathFunctions()
                .declarePhysicsConstants()
                .declareFunction(new EitherFct())
                .build();

// ... later, during runtime evaluation ...

// 2. Create a lightweight child context that inherits EVERYTHING from baseCtx
// This is the idiomatic way to add scoped variables without mutating the base context.
NExprContext scopedCtx = baseCtx.childContext()
        .declareConstant("user_id", 12345)
        .declareConstant("session_timeout", 300)
        // You can even override a parent function just for this scope!
        .declareFunction(new CustomScopedFunction())
        .build(); // Returns an immutable child context

// 3. Evaluate using the child context
// The engine will find 'user_id' in the child, and 'sqrt' in the parent!
NOptional<Object> result = scopedCtx.parse("sqrt(user_id) + session_timeout").get().eval(scopedCtx);
```

#### Why is childContext() the preferred pattern?
- Performance: You don't pay the cost of re-registering hundreds of built-in functions and constants for every evaluation.
- Thread Safety: Both the base context and the child context are immutable. You can safely pass scopedCtx to different threads without synchronization issues.
- Clean Scoping: If a variable is not found in the child context, it seamlessly falls back to the parent context, mimicking standard lexical scoping rules found in modern programming languages.


### Step 8.4: Parsing and Evaluating
With the context fully configured, parsing and evaluation are straightforward. The engine returns NOptional objects, allowing us to handle syntax errors and evaluation failures gracefully without relying on exceptions.

```
String expressionStr = "cpu_load > 0.8 && mem_free < 1024";

// 1. Parse the string into an AST (Abstract Syntax Tree)
NOptional<NExprNode> parsed = ctx.parse(expressionStr);
if (!parsed.isPresent()) {
    NErr.println("Syntax Error in expression!");
    return;
}

// 2. Evaluate the AST against the context
try {
    NOptional<Object> result = parsed.get().eval(ctx);
    if (!result.isPresent()) {
        NErr.println("Evaluation failed (e.g., missing variable).");
        return;
    }
    
    NOut.println(NMsg.ofC("Result: %s", result.get()));
} catch (Exception e) {
    NErr.println(NMsg.ofC("Runtime error: %s", e.getMessage()));
}
```

### Step 8.5: Implementing the eval Command (Full Integration)
Let's integrate this powerful engine into our NutsAdminCLI. We will update the eval command to use the new Context Builder, allowing users to leverage math, physics, custom functions, and our new -> range operator.
Update your `executeEvalCommand` method:


```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.util.List;
import java.util.Random;

@NAppDefinition
public class NutsAdminCLI {
    // ... (previous code remains the same)

    private static final Random random = new Random();

    private void executeEvalCommand(List<String> commands) {
        if (commands.size() < 2) {
            NErr.println(NMsg.ofStyled("Error: Missing expression. Usage: nuts-admin-cli eval \"<expression>\"", NTextStyle.error()));
            return;
        }

        String expressionStr = commands.get(1);
        log.info(NMsg.ofC("Evaluating expression: %s", expressionStr).withIntent(NLogIntent.READ));
        NOut.println(NMsg.ofStyled("=== Advanced Expression Evaluator ===", NTextStyle.primary1()));

        // 1. Build the Context with Built-ins and Variable Resolution
        NExprContextBuilder builder = NExprContextBuilder.of()
            .declareBuiltins()
            .declareMathConstants()
            .declareMathFunctions()
            .declarePhysicsConstants()
            .declareVars(varName -> {
                switch (varName) {
                    case "cpu_load": return NOptional.of(NExprNodeValue.of(random.nextDouble()));
                    case "mem_free": return NOptional.of(NExprNodeValue.of(random.nextInt(4000) + 1000));
                    case "uptime":   return NOptional.of(NExprNodeValue.of(86400));
                    default:         return NOptional.ofNamedEmpty("var " + varName);
                }
            });

        NExprMutableContext ctx = builder.buildMutable();
        ctx.declareConstant("max_threshold", 0.95);

        // Register Custom Function: either(a, b, c) -> returns first non-blank
        ctx.declareFunction(new EitherFct());

        // Register Custom Operator: -> (Range)
        NExprOperator rangeOp = NExprOperator.of("->", NExprOpType.INFIX, NExprOpPrecedence.ASSIGN + 1, NOperatorAssociativity.LEFT, opCtx -> {
            int o1 = NLiteral.of(opCtx.arg(0).get().eval(opCtx)).asInt().orElse(0);
            int o2 = NLiteral.of(opCtx.arg(1).get().eval(opCtx)).asInt().orElse(o1);
            return NArrays.range(o1, o2);
        });
        ctx.declareOperator(rangeOp);

        // 2. Parse and Evaluate
        NOptional<NExprNode> parsed = ctx.parse(expressionStr);
        if (!parsed.isPresent()) {
            NErr.println(NMsg.ofStyled("Error: Failed to parse expression. Check syntax (use && instead of AND).", NTextStyle.error()));
            return;
        }

        try {
            NOptional<Object> result = parsed.get().eval(ctx);
            if (!result.isPresent()) {
                NErr.println(NMsg.ofStyled("Error: Evaluation returned empty (missing variable?).", NTextStyle.error()));
                return;
            }

            NOut.println(NMsg.ofC("Expression: ##%s##", expressionStr));
            NOut.println(NMsg.ofC("Result:     ##%s##", NMsg.ofStyled(String.valueOf(result.get()), NTextStyle.success())));
            
            log.info(NMsg.ofC("Evaluation successful: %s", result.get()).withIntent(NLogIntent.SUCCESS));
        } catch (Exception e) {
            NErr.println(NMsg.ofStyled("Error during evaluation: " + e.getMessage(), NTextStyle.error()));
            log.error(NMsg.ofC("Eval failed: %s", e.getMessage()).withIntent(NLogIntent.FAIL));
        }
    }

    // Custom Function Implementation
    private static class EitherFct extends BaseNexprNExprFct {
        public EitherFct() { super("either"); }

        @Override
        public Object eval(NExprCallContext callContext) {
            for (NExprNodeValue arg : callContext.args()) {
                Object val = arg.value().ifErrorThrow().orNull();
                if (!NBlankable.isBlank(val)) {
                    return val;
                }
            }
            return null;
        }
    }

    private double getCurrentCpuLoad() { return random.nextDouble(); }
    private int getFreeMemory() { return random.nextInt(4000) + 1000; }

    // ... (other commands and lifecycle hooks)
}
```

### Step 8.6: Advanced Templating and Dynamic Generation with NExprTemplate
When you need to generate entire documents or complex multi-line outputs based on runtime data, string concatenation is not enough. You need control flow (loops, conditionals) and dynamic expression evaluation.
NAF solves this with NExprTemplate. It provides a polyglot templating engine that supports multiple syntax styles, all backed by the same powerful NExpr evaluation engine we learned about in Step 8.2.

1. Multiple Syntax Styles
   Unlike templating libraries that lock you into one syntax, NExprTemplate adapts to your preferences and use case
```java
NExprTemplate templateEngine = NExprContextBuilder.of()
    .declareBuiltins()
    .declareVars(NExprVarResolver.ofMap(metrics))
    .build()
    .ofTemplate();

// Choose your preferred syntax:
templateEngine.withMoustacheStyle();  // {{expression}} - Web developers
templateEngine.withJspStyle();        // <%=expression%> - Java developers  
templateEngine.withBashStyle();       // ${expression} - DevOps/Shell scripts
templateEngine.withBoundaries("<%", "%>"); // Custom delimiters
```

2. Example: Bash-Style Templating for DevOps
Let's generate a shell script dynamically using Bash-style syntax, which feels natural for system administration tools:

```java
// Switch to Bash-style syntax
NExprTemplate bashTemplate = NExprContextBuilder.of()
    .declareVars(NExprVarResolver.ofMap(Map.of(
        "hostname", "prod-server-01",
        "port", 8080,
        "services", Arrays.asList("nginx", "postgres")
    )))
    .build()
    .ofTemplate()
    .withBashStyle(); // Uses ${...} syntax

String scriptTemplate = """
    #!/bin/bash
    # Auto-generated deployment script for ${hostname}
    
    echo "Starting services on ${hostname}:${port}"
    
    ${:for svc:services}
    systemctl start ${svc}
    echo "Started ${svc}"
    ${:end}
    
    echo "Deployment complete"
    """;

String renderedScript = bashTemplate.processString(scriptTemplate);
NOut.println(renderedScript);
```

Output:
```
#!/bin/bash
# Auto-generated deployment script for prod-server-01

echo "Starting services on prod-server-01:8080"

systemctl start nginx
echo "Started nginx"
systemctl start postgres
echo "Started postgres"

echo "Deployment complete"
```

3. Example: JSP-Style for Java Developers
For teams more comfortable with Java web syntax:

```java
NExprTemplate jspTemplate = NExprContextBuilder.of()
    .declareVars(NExprVarResolver.ofMap(Map.of("user", "Alice", "role", "admin")))
    .build()
    .ofTemplate()
    .withJspStyle(); // Uses <%=...%> syntax

String jspTemplateStr = """
    Welcome, <%=user%>!
    Your role is: <%=role%>
        <%:if role == "admin"%>
    [Admin Panel Access Granted]
        <%:end%>
    """;

NOut.println(jspTemplate.processString(jspTemplateStr));
```


4. Compilation for Performance
For templates that are reused frequently (e.g., generating reports in a loop), you can compile the template once and reuse it:

```java
// Compile once
NExprCompiledTemplate compiled = templateEngine.compile(templateString);

// Reuse many times with different contexts
for (User user : users) {
    NExprContext userContext = baseContext.childContext()
        .declareVars(NExprVarResolver.ofMap(Map.of("user", user)))
        .build();
    
    String output = compiled.process(userContext);
    // ... use output
}
```

Why This Flexibility Matters:

- Team Familiarity: Java teams can use JSP-style, DevOps teams can use Bash-style, frontend teams can use Mustache-style.
- Context-Appropriate: Generate shell scripts with Bash syntax, HTML with Mustache, or Java code with JSP syntax.
- Zero Lock-in: Switch styles based on the output format or team preference.
- Custom Domains: Use withBoundaries() for DSL-specific syntax.



### Step 8.7: Testing the Expression Engine
Rebuild and reinstall your application:
```bash
mvn clean install
nuts reinstall com.mycompany.admin:nuts-admin-cli
```

Now, let's test the advanced capabilities:
1. Math, Physics, and Logic (C/Java Syntax):
```bash
nuts nuts-admin-cli eval "sqrt(PI) * 2 > 3 && cpu_load < max_threshold"
```

Output:
```
=== Advanced Expression Evaluator ===
Expression: sqrt(PI) * 2 > 3 && cpu_load < max_threshold
Result:     true
```

2. Custom Function (either):

```bash
nuts nuts-admin-cli eval "either('', null, 'fallback_value')"
```

Output:

```
=== Advanced Expression Evaluator ===
Expression: either('', null, 'fallback_value')
Result:     fallback_value
```
3. Custom Operator (-> for Range):

```bash
nuts nuts-admin-cli eval "1 -> 5"
```

Output:

```
=== Advanced Expression Evaluator ===
Expression: 1 -> 5
Result:     [1, 2, 3, 4, 5]
```

4. Handling Syntax Errors (No AND support):

```bash
nuts nuts-admin-cli eval "cpu_load > 0.5 AND mem_free < 2000"
```
Output:

```
=== Advanced Expression Evaluator ===
Error: Failed to parse expression. Check syntax (use && instead of AND).
```

### Summary of Module 8

We have integrated a highly extensible, context-aware expression engine into our CLI.

- We used NExprContextBuilder to inject rich built-ins (Math, Physics) and dynamic runtime variables.
- We implemented a Custom Function (either) by extending BaseNexprNExprFct.
- We defined a Custom Infix Operator (->) using NExprOperator, demonstrating how to extend the language's grammar natively.
- We utilized the robust NOptional-based parsing and evaluation flow to handle errors gracefully.


## Module 9: Runtime Dependency Resolution and Dynamic Classloading
**Objective:** Unlock the ultimate "Nuts Superpower" by dynamically fetching, resolving, and executing third-party Maven artifacts at runtime. We will add a plugin command to our CLI that allows users to specify any Java tool via its Maven coordinates (Group:Artifact:Version), and Nuts will automatically download it, resolve its transitive dependencies, and execute it on the fly—all without the tool being present in our pom.xml.

## Step 9.1: The "Zero-Dependency" Deployment Philosophy##
In traditional Java development, if your application has an optional feature (e.g., a specific database driver, a heavy XML validator, or a specialized network scanner), you must include it in your pom.xml. This bloats your deployment artifact and risks "dependency hell" (classpath conflicts).
Because Nuts is fundamentally a runtime package manager, it allows you to deploy your application as a tiny, bare JAR (often just a few kilobytes). When the user invokes an optional feature, Nuts intercepts the request, queries Maven Central (or your private repositories), downloads the required artifact and its transitive dependencies into the local Nuts cache, and executes it in an isolated classloader. 

## Step 9.2: Identifying Artifacts with NId
To request a dependency at runtime, we use NId, NAF's representation of a Maven coordinate. The standard format is `groupId:artifactId#version` (or `groupId:artifactId:classifier#version`).

```java
// Define the coordinate for a third-party tool
NId toolId = NId.of("org.apache.commons:commons-text#1.10.0");

// You can also use version ranges or dynamic resolution
NId latestTool = NId.of("com.mycompany:diagnostic-plugin"); // Resolves to the latest version
```

### Step 9.3: Executing Remote Artifacts Directly via NExec
The most mind-blowing feature of NAF is that you don't even need to manually download the JAR to run it. The NExec engine (covered in Module 7) natively understands Maven coordinates. If you pass an NId as the command, Nuts handles the entire resolution, download, and execution pipeline transparently.

```java
// Execute a remote Java artifact directly from Maven Central!
// Nuts will download 'com.mycompany:my-remote-tool#1.0.0' and all its dependencies, 
// build the classpath, find the main class, and execute it.
String result = NExec.of()
    .command("com.mycompany:my-remote-tool#1.0.0")
    .command("--help")
    .failFast()
    .grabbedOut();

NOut.println(result);
```

### Step 9.4: Controlling the Fetch Strategy
When resolving these dynamic dependencies, you can control how Nuts searches for them using the NSession's fetch strategy (as detailed in the NAF documentation). This is crucial for enterprise environments where you might want to enforce offline-only builds or prioritize remote repositories.

```java
NSession session = NSession.of();

// Force Nuts to search local and remote repositories concurrently
session.setFetchStrategy(NFetchStrategy.ANYWHERE);

// Or enforce strict offline mode (only use the local Nuts cache)
session.setFetchStrategy(NFetchStrategy.OFFLINE);

// Or force a remote-first search
session.setFetchStrategy(NFetchStrategy.REMOTE);
```

### Step 9.5: Implementing the plugin Command

Let's integrate this into our NutsAdminCLI. We will add a plugin command that takes a Maven coordinate as the first argument, and passes all subsequent arguments directly to that dynamically loaded tool.
Update your run() method's switch statement:

```java
switch (command) {
    case "status": executeStatusCommand(verbose.get()); break;
    case "check": executeCheckCommand(verbose.get()); break;
    case "config": executeConfigCommand(); break;
    case "sync": executeSyncCommand(); break;
    case "inspect": executeInspectCommand(); break;
    case "eval": executeEvalCommand(commands); break;
    case "plugin": executePluginCommand(commands); break; // NEW
    default: NErr.println(NMsg.ofC("Unknown command: ##%s##", command));
}
```

Now, implement the executePluginCommand method:

```java
package com.mycompany.admin;

import net.thevpc.nuts.*;
import java.util.ArrayList;
import java.util.List;

@NAppDefinition
public class NutsAdminCLI {
    // ... (previous code remains the same)

    /**
     * Implements the 'plugin' command using NExec and dynamic Maven resolution.
     */
    private void executePluginCommand(List<String> commands) {
        if (commands.size() < 2) {
            NErr.println(NMsg.ofStyled("Error: Missing plugin coordinate.", NTextStyle.error()));
            NErr.println(NMsg.ofStyled("Usage: nuts-admin-cli plugin <groupId:artifactId#version> [args...]", NTextStyle.comment()));
            return;
        }

        // 1. Extract the Maven coordinate (e.g., "com.example:my-tool#1.0.0")
        String pluginCoordinate = commands.get(1);
        
        log.info(NMsg.ofC("Resolving and executing plugin: %s", pluginCoordinate).withIntent(NLogIntent.START));
        NOut.println(NMsg.ofStyled("=== Dynamic Plugin Executor ===", NTextStyle.primary1()));
        NOut.println(NMsg.ofC("Resolving ##%s## from repositories...", pluginCoordinate));

        // 2. Ensure we are allowed to fetch from remote repositories if not cached
        NSession session = NSession.of();
        if (!session.isCached()) {
            session.setFetchStrategy(NFetchStrategy.ANYWHERE);
        }

        // 3. Build the NExec command dynamically
        NExec exec = NExec.of()
            .command(pluginCoordinate) // Nuts recognizes this as a Maven GAV!
            .grabOut() // mark as grabbed
            .grabErr() // mark as grabbed
            .failFast(false); // We want to capture the plugin's output even if it exits with non-zero

        // 4. Pass all remaining CLI arguments to the plugin
        for (int i = 2; i < commands.size(); i++) {
            exec.command(commands.get(i));
        }

        // 5. Execute and capture output
        try {
            NTrace.println("Starting remote execution...");
            String stdout = exec.grabbedOut();
            String stderr = exec.grabbedErr();
            int exitCode = exec.getResultCode();

            if (!stdout.isEmpty()) {
                NOut.println(NMsg.ofStyled("--- Plugin Output ---", NTextStyle.primary2()));
                NOut.println(stdout);
            }
            if (!stderr.isEmpty()) {
                NErr.println(NMsg.ofStyled("--- Plugin Errors ---", NTextStyle.warn()));
                NErr.println(stderr);
            }

            NOut.println();
            if (exitCode == 0) {
                NOut.println(NMsg.ofStyled("Plugin execution completed successfully.", NTextStyle.success()));
            } else {
                NOut.println(NMsg.ofC("Plugin exited with code: ##%d##", exitCode));
            }

            log.info(NMsg.ofC("Plugin execution finished with exit code %d", exitCode).withIntent(NLogIntent.SUCCESS));
        } catch (Exception e) {
            NErr.println(NMsg.ofStyled("Error: Failed to resolve or execute the plugin.", NTextStyle.error()));
            NErr.println(NMsg.ofC("Details: %s", e.getMessage()));
            log.error(NMsg.ofC("Plugin execution failed: %s", e.getMessage()).withIntent(NLogIntent.FAIL));
        }
    }

    // ... (other commands and lifecycle hooks)
}
```

### Step 9.6: Testing the Dynamic Resolution
Rebuild and reinstall your application:
```bash
mvn clean install
nuts reinstall com.mycompany.admin:nuts-admin-cli
```

Now, let's test the dynamic resolution engine. For this example, let's assume we want to run a hypothetical diagnostic tool hosted on Maven Central. 

1. Executing a Remote Plugin:

```bash
nuts nuts-admin-cli plugin "com.example:network-scanner#2.1.0" --target=192.168.1.1 --verbose
```

What happens under the hood when you press Enter:

- Nuts parses com.example:network-scanner#2.1.0.
- It checks the local Nuts cache (~/.cache/nuts/...).
- If missing, it queries Maven Central for the POM, resolves the transitive dependency tree (e.g., downloading commons-net, slf4j-api, etc.).
- It downloads all required JARs into the isolated Nuts workspace.
- It constructs a temporary, isolated classpath.
- It locates the Main-Class defined in the JAR's MANIFEST.MF and executes it, passing --target=192.168.1.1 --verbose.
- It captures the standard output and returns it to our NutsAdminCLI.

2. Observing the Resolution Trace:
If you run the command with --trace, you will see the exact resolution pipeline:
```bash
nuts nuts-admin-cli --trace plugin "com.example:network-scanner#2.1.0"
```

Output:

```
=== Dynamic Plugin Executor ===
Resolving com.example:network-scanner#2.1.0 from repositories...
[TRACE] Searching for com.example:network-scanner#2.1.0 in local cache...
[TRACE] Not found. Querying remote repository: https://repo1.maven.org/maven2/
[TRACE] Resolving transitive dependencies...
[TRACE] Downloading com.example:network-scanner-2.1.0.jar (45 KB)
[TRACE] Downloading commons-net:commons-net#3.8.0 (310 KB)
[TRACE] Building isolated classpath...
Starting remote execution...
--- Plugin Output ---
Scanning 192.168.1.1...
Found 3 open ports.
```

### Step 9.7: Programmatic Classloading (For Libraries)

While `NExec` is perfect for running standalone tools, what if you need to use a downloaded JAR as a library inside your own Java code?
NAF provides the `NClassLoader` (or workspace classpath manipulation) to dynamically inject JARs into the current JVM's execution context.

```java
// 1. Define the dependency
NId libId = NId.of("com.google.code.gson:gson#2.10.1");

// 2. Fetch the artifact paths (downloads if necessary)
// Nuts returns a list of NPaths representing the JAR and all its transitive dependencies

// 3. Create an isolated ClassLoader
try (NClassLoader classLoader = NSession.of().search().addId(libId).getResultClassLoader()) {
    
    // 4. Load the class dynamically
    Class<?> gsonClass = classLoader.loadClass("com.google.gson.Gson");
    
    // 5. Instantiate and invoke via Reflection
    Object gsonInstance = gsonClass.getDeclaredConstructor().newInstance();
    Method toJsonMethod = gsonClass.getMethod("toJson", Object.class);
    
    String json = (String) toJsonMethod.invoke(gsonInstance, Map.of("status", "success"));
    NOut.println(NMsg.ofC("Dynamically loaded Gson produced: %s", json));
    
} catch (Exception e) {
    log.error(NMsg.ofC("Dynamic classloading failed: %s", e));
}
```

> Note: While NExec is the recommended approach for running external tools, NClassLoader is available when you absolutely must integrate a dynamically downloaded library directly into your application's logic.

### Summary of Module 9
We have unlocked the true power of the Nuts ecosystem.

- We utilized NId to define Maven coordinates dynamically.
- We leveraged NExec to transparently download, resolve, and execute remote Java artifacts without a single line of classpath boilerplate.
- We controlled the resolution behavior using NFetchStrategy on the NSession.
- We demonstrated how to use NClassLoader to programmatically inject downloaded libraries into the running JVM.

By moving dependency resolution to runtime, our NutsAdminCLI remains incredibly lightweight, while possessing the ability to dynamically adopt any tool in the Maven ecosystem on demand.

## Module 10: Enterprise Integration: Spring Boot & SLF4J

**Objective:** Bridge our standalone NAF application into the enterprise Spring ecosystem. We will demonstrate how to seamlessly integrate NAF's powerful runtime utilities (NWorkspace, NExec, NPath) into Spring Boot, inject them into Spring Beans, and unify NAF's semantic NLog with the industry-standard SLF4J/Logback infrastructure.

### Step 10.1: The Philosophy of Integration
As we established in Module 1, NAF is fundamentally a standalone, zero-dependency framework. You do not need Spring to use NAF.
However, in enterprise environments, Spring Boot and SLF4J are ubiquitous. NAF is designed to play beautifully with them. By adding just two lightweight bridge dependencies, you can:

- Inject NAF's session-aware components (NSession, NWorkspace) directly into Spring @Component beans.
- Route NAF's rich, structured NLog (with its NMsgIntent semantics and NMsg formatting) directly into your existing SLF4J/Logback configuration.
- Use NAF's NBeanRef to safely reference Spring-managed beans inside NAF's concurrency primitives (like NRetryCall or NSaga) without serialization issues.


### Step 10.2: Maven Configuration
To enable Spring Boot and SLF4J integration, we add the respective NAF bridge modules to our pom.xml.


```xml
<dependencies>
    <!-- Standard Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- NAF Core -->
    <dependency>
        <groupId>net.thevpc.nuts</groupId>
        <artifactId>nuts</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- NAF Spring Boot Bridge -->
    <dependency>
        <groupId>net.thevpc.nuts</groupId>
        <artifactId>nuts-spring-boot</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- NAF SLF4J Bridge (Routes NLog to SLF4J/Logback) -->
    <dependency>
        <groupId>net.thevpc.nuts</groupId>
        <artifactId>nuts-slf4j</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Step 10.3: Bootstrapping the Hybrid Application
To merge the NAF lifecycle with the Spring Boot lifecycle, we annotate our main class with both @SpringBootApplication and @NAppDefinition, and import the NAF Spring configuration.

```java
package com.mycompany.admin.spring;

import net.thevpc.nuts.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@NAppDefinition
@SpringBootApplication
@Import(NutsSpringBootConfig.class) // Crucial: Wires NAF components into the Spring Context
public class NutsAdminSpringApp {

    public static void main(String[] args) {
        // Spring Boot handles the startup, which in turn initializes the NAF Workspace
        SpringApplication.run(NutsAdminSpringApp.class, args);
    }

    /**
     * This NAF lifecycle hook runs AFTER the Spring ApplicationContext is fully initialized.
     * It allows you to execute NAF-specific CLI logic or startup routines within the Spring environment.
     */
    @NAppRunner
    public void run() {
        NSession session = NSession.of();
        NOut.println(NMsg.ofStyled(">> NAF is running seamlessly inside Spring Boot!", NTextStyle.success()));
        
        // You can still parse CLI arguments passed to the Spring Boot app
        NCmdLine cmdLine = NApp.of().cmdLine();
        // ... handle custom NAF flags ...
    }
}
```

### Step 10.4: Injecting NAF Components into Spring Beans
Thanks to `@Import(NutsSpringBootConfig.class)`, NAF's core services are now available as Spring Beans. You can inject them directly into your @Service or @Controller classes.
Note: NSession is thread-scoped. Spring's integration ensures that when you inject NSession, you get a proxy that correctly resolves to the current thread's execution context.

```java
package com.mycompany.admin.spring;

import net.thevpc.nuts.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticsService {

    // Inject NAF components directly!
    @Autowired private NSession session;
    @Autowired private NWorkspace workspace;
    @Autowired private NExec exec;

    public String getRemoteServerStatus(String sshTarget) {
        // Use the injected, session-aware NExec to run a remote command via SSH
        // The session automatically handles trace modes, output formats, and timeouts.
        return exec.at(sshTarget)
            .command("systemctl", "status", "nginx")
            .failFast(false)
            .grabbedOut(); // implicitly includes grabOut().run()
    }
    
    public String getConfigPath() {
        // Use the injected workspace/session to resolve XDG-compliant paths
        return NPath.of(NStoreKey.of(NStoreScope.USER, NStoreType.CONFIG, NApp.of().getId()))
                    .toString();
    }
}
```

### Step 10.5: Unifying Logging (NLog -> SLF4J)

The true magic of the nuts-slf4j bridge is that you do not need to change your NAF logging code.
All your existing NLog calls—complete with NMsg formatting, NLogIntent semantics, and Scoped Logging—will automatically be routed through SLF4J to your configured backend (e.g., Logback). This means your enterprise logging aggregators (like ELK or Splunk) will receive NAF's rich, structured logs.

```java
package com.mycompany.admin.spring;

import net.thevpc.nuts.*;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

    // NLog automatically delegates to SLF4J/Logback under the hood!
    private static final NLog log = NLog.of(OrderProcessingService.class);

    public void processOrder(String orderId) {
        long startTime = System.currentTimeMillis();

        // 1. Semantic Logging with NMsg formatting
        // This will appear in Logback with the formatted message and the START intent marker
        log.info(NMsg.ofC("Processing order #%s", orderId)
            .withIntent(NLogIntent.START));

        try {
            // ... business logic ...
            simulateWork();

            long duration = System.currentTimeMillis() - startTime;
            
            // 2. Attach execution duration and SUCCESS intent
            log.info(NMsg.ofC("Order #%s completed successfully", orderId)
                .withIntent(NLogIntent.SUCCESS)
                .withDurationMs(duration));

        } catch (Exception ex) {
            // 3. Attach exceptions and FAIL intent
            log.error(NMsg.ofC("Payment failed for order #%s", orderId)
                .withIntent(NLogIntent.FAIL)
                .withThrowable(ex));
        }
    }

    private void simulateWork() {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
    }
}
```

### Step 10.6: Scoped Logging across Spring Beans

One of the most powerful features of NAF is Scoped Logging (using NLog.ofScoped()). When integrated with Spring, you can establish a logging scope at the Controller/API layer, and all downstream Spring Services that use NLog.ofScoped() will automatically inherit the context (prefixes, placeholders, and custom handlers) without polluting the global MDC.

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderProcessingService orderService;
    @Autowired private InventoryService inventoryService;

    @PostMapping("/{orderId}")
    public String handleOrder(@PathVariable String orderId, @RequestHeader("X-User-Id") String userId) {
        
        // Establish a NAF Logging Scope for this specific HTTP Request
        NLog.runInScope(
            NLogScope.of()
                .withMessagePrefix(NMsg.ofC("[API-REQ-%s] ", orderId))
                .withPlaceholder("userId", userId)
                .withPlaceholder("orderId", orderId),
            () -> {
                // All NLog.ofScoped() calls inside these Spring Services 
                // will automatically include the [API-REQ-123] prefix and $userId placeholder!
                
                inventoryService.reserveStock(); 
                orderService.processOrder(orderId);
            }
        );
        
        return "Order Processed";
    }
}

@Service
public class InventoryService {
    // EXPLICITLY opt-in to the scoped context
    private static final NLog scopedLog = NLog.ofScoped(InventoryService.class);

    public void reserveStock() {
        // Output in Logback: [API-REQ-123] Reserving stock for user U-99 and order 123
        scopedLog.info(NMsg.ofV("Reserving stock for user $userId and order $orderId")
            .withIntent(NLogIntent.UPDATE));
    }
}
```

### Step 10.7: Referencing Spring Beans inside NAF Concurrency (Advanced)

Sometimes, you want to use NAF's powerful NRetryCall or NSaga engines, but the actual work needs to be done by a Spring-managed Bean (e.g., a @Transactional service).
NAF provides NBeanRef to safely reference Spring beans. The reference itself is serializable and lightweight; the actual Spring bean is resolved lazily from the Spring Context only when the method is invoked.

```java
@Service
public class ResilientSpringWorkflow {

    @Autowired private NConcurrent concurrent;
    
    // Reference a Spring Bean by its name and interface
    // This does NOT trigger bean resolution yet. It's just a safe, serializable pointer.
    private final NBeanRef<PaymentGateway> paymentGatewayRef = NBeanRef.of("paymentGatewayImpl").as(PaymentGateway.class);

    public void executeResilientPayment(String orderId) {
        
        // Create a NAF Retry Call that delegates to the Spring Bean
        concurrent.retryCallFactory()
            .of("payment-retry", () -> {
                // The Spring Bean is resolved HERE, from the active ApplicationContext
                return paymentGatewayRef.charge(orderId, 100.0); 
            })
            .setMaxRetries(3)
            .setExponentialRetryPeriod(Duration.ofSeconds(1), 2.0)
            .call();
    }
}
```


### Summary of Module 10 & The Tutorial

We have successfully taken our NutsAdminCLI from a standalone, zero-dependency package manager-aware tool, and bridged it into the enterprise Java ecosystem.

- Standalone Power: Modules 1-9 proved that NAF provides a complete, production-ready foundation (CLI, I/O, Filesystems, Resilience, Expressions, Dynamic Classloading) without forcing you to adopt a massive framework like Spring.
- Seamless Integration: By adding nuts-spring-boot and nuts-slf4j, we unlocked the ability to inject NAF's session-aware components into Spring Beans and route NAF's semantic, structured logs directly into Logback/SLF4J.
- The Best of Both Worlds: You retain NAF's deterministic workspace constraints, tri-state error modeling (NOptional), and zero-boilerplate CLI parsing, while leveraging Spring's massive ecosystem of starters, AOP, and dependency injection.

### Final Thoughts on the Nuts Application Framework
Throughout this tutorial, we've seen that Nuts is not just a package manager. It is a comprehensive, battle-tested Application Framework born out of the necessity to build a robust, zero-dependency runtime environment.
By standardizing how Java applications handle I/O, configuration, cross-platform execution, and resilience, NAF allows you to focus entirely on your business logic, whether you are building a lightweight CLI tool, a DevOps automation script, or a massive enterprise Spring Boot microservice.
