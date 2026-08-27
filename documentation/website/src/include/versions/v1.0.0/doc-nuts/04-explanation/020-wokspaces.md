---
title: Workspace
---

# Workspace

A fundamental concept in **nuts** is the **Workspace**. A workspace is a self-contained, isolated environment that manages repositories, installed applications, downloaded libraries, configurations, and security settings.

If you are familiar with Python's `virtualenv` or Node's `node_modules`, a **nuts** workspace provides a similar, but more comprehensive, level of isolation for the Java ecosystem.

## What is a Workspace?

A workspace acts as the root boundary for execution. When you run a command via **nuts**, the runtime operates exclusively within the context of the active workspace. It dictates:

- Which repositories are queried for packages.
- Which versions of Java (JDKs/JREs) are available or auto-provisioned.
- Where downloaded JARs and artifacts are stored.
- What configurations and aliases are available.
- What security permissions and authentication rules apply.

By default, **nuts** operates in the `default-workspace`, providing a seamless "just works" experience without needing upfront configuration.

## Workspace Anatomy

Internally, a workspace consists of several distinct logical areas, heavily inspired by the XDG Base Directory Specification:

- **Config**: Stores workspace settings, secure credentials, aliases, and custom repositories.
- **Bin**: Holds installed application binaries, boot scripts, and launchers.
- **Lib**: The central repository cache for downloaded dependencies (JARs, POMs).
- **Var**: Application-specific persistent data.
- **Log**: Centralized logging for the package manager and managed applications.
- **Cache**: Ephemeral storage for optimized downloads and index caches.
- **Run**: Runtime files, sockets, and named pipes (often cleared on reboot)

## Storage Strategies

**nuts** allows you to configure how a workspace's internal anatomy maps to the host operating system's file system. It offers two primary strategies:

### Exploded Strategy (Default)
In the Exploded strategy, **nuts** scatters the workspace directories across the host OS using standard conventions (XDG on Linux/macOS, AppData on Windows).
- Configuration goes to `~/.config/nuts/...`
- Cache goes to `~/.cache/nuts/...`
- Logs go to `~/.local/log/nuts/...`

**Why use it?** It integrates perfectly with OS backup tools, allows cache directories to be placed on faster ephemeral storage, and respects native platform conventions.

### Standalone Strategy
In the Standalone strategy, the entire workspace is confined to a single directory tree. Everything (config, cache, apps, logs) is stored directly under one root folder.

**Why use it?** It is ideal for portability. You can create a Standalone workspace on a USB drive, within a Docker volume, or in a roaming profile. Deleting or moving the workspace is as simple as deleting or moving the root folder.

## Workspace Lifecycle

1. **Creation**: A workspace is initialized either automatically on first use or explicitly via `nuts -w <name> workspace init`.
2. **Configuration**: Repositories are added, Java environments are scanned or provisioned, and security settings are established.
3. **Usage**: Applications are installed, updated, and executed within the boundary.
4. **Maintenance/Reset**: A workspace can be completely wiped clean without affecting other workspaces or the host OS.

## Isolation Guarantees

Workspaces provide strict isolation guarantees. 

- **No Cross-Contamination**: Installing an artifact in Workspace A has absolutely no effect on Workspace B. They maintain separate caches, configurations, and runtime classpaths.
- **Environment Targeting**: You can configure a `dev` workspace pointing to snapshot repositories, and a `prod` workspace locked to stable internal repositories.
- **Safe Experimentation**: If an application installation breaks or corrupts a workspace, you can simply delete the workspace without risking system stability.

## System-Wide vs Per-User

By default, workspaces are per-user, isolated within the user's home directory. This allows standard users to install and manage software without requiring `sudo` or Administrator privileges.

For server environments or system-wide software distribution, **nuts** can run in system-mode using the `-g` flag. This initializes a system-wide workspace (e.g., in `/etc/nuts` and `/var/lib/nuts`), allowing all users on the OS to access the installed tools.

## Named and Temporary Workspaces

You can easily switch between workspaces using the `-w` or `--workspace` flag:

```bash
# Execute in a specific named workspace
nuts -w testing-env install my-app

# Create and use a temporary, ephemeral workspace
nuts -w temp-$$ run my-experimental-app
```

Temporary workspaces are incredibly powerful in CI/CD pipelines, ensuring every pipeline run executes in a pristine, guaranteed-clean environment, avoiding tricky cache-poisoning issues.
