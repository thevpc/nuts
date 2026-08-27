---
id: cmds
title: Nuts Commands
sidebar_label: Nuts Commands
---

**nuts** provides a rich set of commands to manage artifacts, repositories, and workspace configurations. Commands are categorized into internal commands (built into the runtime) and external commands (provided by downloaded artifacts).

## Command Categories

### Package Management
* `install`: Installs an artifact and its dependencies into the workspace.
* `uninstall`: Removes an installed artifact.
* `reinstall`: Reinstalls an existing artifact, re-resolving dependencies.
* `update`: Checks for and applies updates to installed artifacts.
* `check-updates`: Checks for newer versions without applying them.

### Artifact Transfer
* `fetch`: Downloads and caches artifacts without installing them.
* `deploy`: Deploys a local artifact into a local repository.
* `undeploy`: Removes an artifact from a local repository.
* `push`: Synchronizes a local repository to its remote peer.
* `bundle`: Creates an air-gapped standalone bundle containing a complete workspace.

### Discovery & Execution
* `search`: Searches for artifacts across local and remote repositories.
* `which`: Resolves the appropriate application or command for execution.
* `exec`: Executes an artifact or internal command.

### Configuration
* `settings`: Main configuration command to manage workspace settings, repositories, aliases, launchers, and security.

### Information
* `info`: Displays detailed workspace and installation information.
* `version`: Shows the nuts API and runtime versions.
* `help`: Provides documentation and usage instructions.
* `license`: Displays the nuts open-source license.
* `welcome`: Bootstraps the workspace and shows a welcome message.

## Execution Modes

Nuts supports multiple ways to invoke external commands:

* **Spawn** (`--spawn`): The default mode. Spawns a new operating system process for the command.
* **Embedded** (`--embedded`): Executes Java-based commands within the current JVM process to avoid startup overhead.
* **System** (`--syscall`): Delegates the execution directly to the underlying operating system.

Additionally, commands can be run in **dry mode** using the `--dry` flag. This simulates the execution without applying any side effects, allowing you to preview actions before committing.

## Common Global Options

Many commands accept global options to modify their behavior:
* `-y` or `--yes`: Automatically confirms all interactive prompts. Ideal for CI/CD environments.
* `-n` or `--no`: Automatically rejects all interactive prompts.
* `--bot`: Enables bot mode, formatting output for automated processing and suppressing interactive prompts.
* `--json`: Formats the output as structured JSON.
* `--workspace=<path>`: Specifies an alternative workspace directory.
* `--trace`: Enables detailed tracing for debugging.
* `--dry`: Simulates the command without side effects.
