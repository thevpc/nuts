---
id: exec-cmd
title: Exec
---

## Synopsis

```sh
nuts exec [options] <command> [<args>...]
```

## Description

The `exec` command executes another artifact or command. When you type `nuts <command>`, it is implicitly translated to `nuts exec <command>`.

The `exec` command is particularly helpful because it permits passing extra parameters to application executors. In **nuts**, an application executor is an artifact that can be used to run other artifacts. For instance, **nsh** (a companion shell) is an executor for all `.nsh` artifacts. 

Some executors are specially handled, such as the `java` executor, which is used to run all JARs and Java-based artifacts. The Java executor supports all standard JVM options.

```bash
nuts exec -Xmx1G netbeans-launcher
```
In this example, the `-Xmx1G` argument is passed to the Java executor because `netbeans-launcher` is resolved as a Java-based artifact.

## External Commands

External commands are commands that invoke another downloaded artifact. For example:

```bash
nuts netbeans-command
```

This runs an external command, specifically the `net.thevpc.app:netbeans-launcher#1.2.2` artifact.

## External Files & URLs

You can run any JAR file using **nuts** as long as it contains a supported descriptor (e.g., standard Maven metadata) and is specified as a path (containing a `/` or `\` separator).

```bash
wget -N https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar
nuts ./netbeans-launcher-1.2.2.jar
```

You can also run a remote file directly via its URL:

```bash
nuts https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar
```

## Execution Types

### Spawn (Default)
The default execution type where any external command spawns a new process. Internal commands are not affected and always run embedded.
```bash
nuts --spawn ls
```

### Embedded
The command runs within the current JVM process, avoiding the overhead of spawning a new process (applicable only to Java commands).
```bash
nuts --embedded nsh -c ls
```

### System
The command execution is delegated directly to the underlying operating system shell.
```bash
nuts --system ls
```

## Rerun & Cron Scheduling

The `exec` command supports repeated execution loops and cron scheduling:

| Option | Description |
|---|---|
| `--rerun` | Runs the command continuously in a loop |
| `--cron=<pattern>` | Schedules application execution using standard cron expression format |
| `--rerun-min-time=<duration>` | Minimum runtime threshold; if app crashes/stops before this time, the loop terminates |
| `--rerun-safe-time=<duration>` | Sleep duration to wait between consecutive loop iterations |
| `--rerun-max-count=<int>` | Maximum number of iterations before exiting the rerun loop |

```bash
# Rerun app with a 5 second safe delay between runs
nuts exec --rerun --rerun-safe-time=5s myapp

# Run on a cron schedule
nuts exec --cron="0 0 * * *" my-daily-job
```

## Remote Execution

Execute commands on remote hosts via connection strings:

| Option | Description |
|---|---|
| `--target=<connection-string>` | Runs the command on a remote machine using the given connection string. Currently supports SSH connection strings in the format `ssh://[username[:password]@]host[:port]`. If username is omitted, current user is used; if password is omitted, default SSH key is used; if port is omitted, 22 is used. |

```bash
nuts exec --target=ssh://user:pass@remote-host:22 myapp
```

## Privilege Modes

| Option | Description |
|---|---|
| `--current-user` | Runs with current user privileges (default) |
| `--as-root` | Runs with system root privileges (prompts for root password if required) |
| `--sudo` | Runs with elevated privileges via `sudo` |
| `--as-user=<username>` | Runs under specified system user identity |

## Java Executor Options

When executing Java artifacts, **nuts** provides specialized executor flags:

| Option | Description |
|---|---|
| `--java-version=<v>` | Specify required Java version (e.g. `11`, `17`, `21`) |
| `--java-home=<path>` | Explicit JDK/JRE installation home directory |
| `--main-class=<class>` | Specify main class to execute (name or candidate index) |
| `--class-path=<cp>` | Custom classpath entries |
| `--nuts-path=<ids>` | Additional nuts artifact IDs to append to classpath |
| `--dir=<path>` | Working directory for process execution |
| `--win` / `--javaw` | Use windowless `javaw` executable for GUI applications |
| `--show-command` | Prints the full resolved Java command line prior to launch |

## Execution Modes

### Effective Execution (Default)
The command is executed normally with all side effects applied.

### Dry Execution
The command is simulated with no side effects.
```bash
nuts --dry version
```
