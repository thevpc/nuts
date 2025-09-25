---
title: Executing External Commands
---


NAF provides a high-level API to execute external commands, whether locally or remotely, in a concise, structured, and cross-platform manner. Using NExecCmd, you can run processes, capture their output, handle errors, and even execute them on remote hosts via SSH—all without the verbosity of the standard Java Process API.
**Key Features**
- Embedded & Local Execution: Run commands in the current JVM or OS environment.
- Remote Execution: Execute commands on SSH-enabled hosts transparently.
- Automatic Output Capture: Grab stdout and stderr without manually reading streams.
- Run Java Artifacts Directly: Download a JAR and its dependencies from Maven or remote repositories and execute it automatically.
- Fail-Fast Control: Stop execution immediately on errors if desired.
- Minimal Boilerplate: Avoid complex Runtime.exec() and thread-based stream consumption.


## To create a new process

```java
// Simple embedded command execution
String result = NExecCmd.of("info")
                .getGrabbedAllString();
NOut.println(result);
```

- `.of("info")` creates the command to execute.
- `.getGrabbedAllString()` captures the full output (stdout + stderr) as a string.
- No exceptions are thrown automatically; the caller can inspect result or `getResultCode()` to decide if the execution succeeded.


## Example: Executing a shell command with NSH

```java
String result = NExecCmd.of()
        .addCommand(NConstants.Ids.NSH, "-c", "ls")
        .grabAll()
        .failFast()
        .getGrabbedOutString();

NOut.println("Result:");
NOut.println(result);
```

- Nuts provides embedded shells (NSH) for running commands in a sandboxed environment.
- Output is automatically sanitized to remove terminal formatting unless explicitly requested.


## Special Executor IDs
Nuts can handle commands with special identifiers or remote resources:


```java
String result = NExecCmd.of()
        .addExecutorOptions("--bot")
        .addCommand("com.mycompany:my-remote-artifact")
        .addCommand("list", "-i")
        .getGrabbedAllString();

NOut.println(result);
```

- Useful for running workspace-bound tools or remote executables.
- Works with Maven coordinates or URLs pointing to executable jars.


## Remote & Structured Command Execution

NExecCmd is not limited to local commands: you can execute processes on remote systems via SSH (or any supported executor) and easily capture stdout/stderr with minimal boilerplate:

```java
NExecCmd u = NExecCmd.of()
        .at("ssh://me@myserver")              // Execute on remote server
        .setIn(NExecInput.ofNull())           // No stdin input
        .addCommand("ps", "-eo",             // Command + arguments
            "user,pid,%cpu,%mem,vsz,rss,tty,stat,lstart,time,command")
        .grabErr()                            // Capture stderr
        .setFailFast(true)                    // Optional: fail immediately on error
        .grabOut();                           // Capture stdout

// Access results
String stdout = u.getGrabbedOutString();
String stderr = u.getGrabbedErrString();
int exitCode = u.getResultCode();

NOut.println("Exit code: " + exitCode);
NOut.println("Output:\n" + stdout);
```

Key points:

- at(...) allows specifying a remote target (SSH, etc.).
- grabOut() / grabErr() capture output streams automatically.
- setFailFast(true) ensures that any failure will stop execution immediately.
- No need for Runtime.exec() boilerplate or manual stream handling.

This makes NExecCmd a powerful, concise, and cross-platform alternative to the traditional Java Process API.

## Why NExecCmd is Better than Runtime.exec()

Using Runtime.exec() requires verbose boilerplate to:
- manage stdin/stdout/stderr streams,
- handle threading to avoid blocking,
- wait for process completion,
- and manually check exit codes.

With NExecCmd, all of this is simplified and made cross-platform, while also supporting structured output handling, remote execution, and direct execution of downloaded Java artifacts transparently.
