---
title: Workspace
---

:::tip What You'll Learn
In this section, you will learn how to manage and isolate your environments using **nuts** workspaces. We will cover:
* What workspaces are and how they provide isolation (similar to Python's virtualenv).
* How to create, switch, and delete workspaces.
* The differences between Exploded and Standalone storage strategies.
* Practical use cases for using multiple workspaces.
:::

## What is a Workspace?

A **workspace** in **nuts** is a completely isolated environment. It contains its own set of installed artifacts, configuration files, caches, configured repositories, and even its own automatically provisioned JDKs. 

If you are familiar with Python's `virtualenv` or Node's `nvm`, a **nuts** workspace serves a similar purpose for Java and JVM applications. By default, anything you install, configure, or run happens inside a workspace. There is no system-wide bleeding of state unless explicitly shared.

### The Default Workspace

When you run **nuts** for the first time, it automatically creates the default workspace. On most systems, the configuration for this default workspace is located at:

```text
~/.config/nuts/default-workspace
```

If you do not specify a workspace in your commands, **nuts** will always use this default environment.

## Creating and Switching Workspaces

You do not need a special command to "create" a workspace. You simply specify the workspace name using the `-w` (or `--workspace`) flag, and **nuts** will create it automatically if it does not exist.

For example, to install an artifact into a brand new workspace named `my-workspace`:

```bash
nuts -w my-workspace install netbeans-launcher
```

To run a command within that workspace, you simply include the flag again:

```bash
nuts -w my-workspace netbeans-launcher
```

Because each workspace is a complete sandbox, applications installed in the default workspace will not be available in `my-workspace` (and vice versa) unless you explicitly install them there.

## Storage Strategies

When you create a new workspace, you can choose how its files are stored on your disk. **nuts** offers two primary storage strategies:

### 1. Exploded Strategy (Default)

The **Exploded** strategy strictly follows the XDG Base Directory Specification on Linux (and equivalent native patterns on Windows and macOS). Files are dispersed across appropriate system directories based on their purpose:

* Configurations go to `~/.config/nuts/`
* Caches and downloads go to `~/.cache/nuts/`
* Installed binaries and libraries go to `~/.local/share/nuts/`
* Logs go to `~/.local/state/nuts/` (or similar logging directories)

This is the recommended strategy for standard desktop usage, as it respects OS-level backup policies and disk usage tools. To explicitly create a workspace with this strategy:

```bash
nuts -w my-workspace --exploded
```

### 2. Standalone Strategy

The **Standalone** strategy keeps everything related to the workspace strictly confined to a single directory folder. 

This strategy is ideal for portability. You can create a standalone workspace on a USB flash drive, take it to another computer, and all your applications, settings, and Java runtimes will function identically without polluting the host machine.

To create a standalone workspace, use the `--standalone` flag and provide a path:

```bash
nuts -w /path/to/portable-workspace --standalone
```

## Workspace Isolation

Each workspace is completely self-contained. When you switch workspaces, you are switching:
* The list of installed applications and their specific versions.
* Repository configurations and credentials.
* Aliases, launchers, and imported group IDs.
* Local Maven/dependency caches.
* Automatically provisioned JDKs.

## Practical Use Cases

Workspaces are incredibly powerful tools for developers and system administrators:

* **Environment Separation:** Maintain separate `dev`, `staging`, and `prod` workspaces to test different configurations or application versions side-by-side.
* **CI/CD Ephemeral Workspaces:** In a Continuous Integration pipeline, you can use a temporary workspace (e.g., `nuts -w temp-$$ ...`) to guarantee a pristine, reproducible environment that is destroyed after the job finishes.
* **Testing Upgrades:** Safely test a new version of a tool in a sandbox workspace without breaking your daily workflow in the default workspace.

This will output a list of available workspaces, along with their paths and storage strategies.

## Resetting a Workspace

If you are whant to reset all of workspace config and files you can use the **reset** mode (`-Z` flag) combined with the workspace flag.

```bash
nuts -ZQ -w my-workspace
```

## Deleting a Workspace

If you are done with a workspace and want to reclaim disk space, you can delete it using the **reset-quit** mode (`-ZQ` flags) combined with the workspace flag.

```bash
nuts -ZQ -w my-workspace
```

## Deleting all common Workspaces, Uninstalling nuts

:::warning
This performs a hard reset/deletion of the specified workspace, removing all installed artifacts, caches, and configurations associated with it. Ensure you are targeting the correct workspace before running this command.
:::

```bash
nuts --reset-hard
```
