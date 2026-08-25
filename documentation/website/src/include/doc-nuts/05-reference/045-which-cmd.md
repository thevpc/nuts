---
id: which-cmd
title: Which
---

## Synopsis

```sh
nuts which [options] <command>
```

## Description

The `which` command resolves the command execution path without actually executing it. It performs a dry run to determine how **nuts** would execute the specified command.

```bash
$ nuts which version ls
version : internal command 
ls : nuts alias (owner maven-local://net.thevpc.app.nuts.toolbox:nsh# ) : maven-local://net.thevpc.app.nuts.toolbox:nsh# -c ls
```
In this example, `which` reveals that `version` is an internal command, while `ls` is a nuts alias mapping to an artifact-based command (`nsh -c ls`).

This is invaluable for debugging why a specific version of a tool is being run, or to inspect alias definitions and executors.

## External Commands

For external commands, `which` resolves the exact artifact ID and version that will be invoked.

```bash
$ nuts which netbeans-command
net.thevpc.app:netbeans-launcher#1.2.2
```

## External Files & URLs

Just like `exec`, `which` can resolve execution for local JAR files and remote URLs, verifying if they contain supported descriptors.

```bash
nuts which ./netbeans-launcher-1.2.2.jar
nuts which https://example.com/myapp.jar
```

## Execution Types

You can use `which` alongside execution type flags to see how execution strategies affect resolution.

* `--spawn`: Resolves for spawning a new process (default).
* `--embedded`: Resolves for running within the current JVM process.
* `--syscall`: Resolves for operating system delegation.

## Execution Modes

Since `which` is fundamentally a diagnostic tool, it operates safely without side effects, similar to running commands with the `--dry` flag.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/which.ntf")}}
``````
