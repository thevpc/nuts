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
nuts --embedded ls
```

### Syscall
The command execution is delegated directly to the underlying operating system.
```bash
nuts --syscall ls
```

## Execution Modes

### Effective Execution (Default)
The command is executed normally with all side effects applied.

### Dry Execution
The command is simulated with no side effects.
```bash
nuts --dry version
```

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/exec.ntf")}}
``````
