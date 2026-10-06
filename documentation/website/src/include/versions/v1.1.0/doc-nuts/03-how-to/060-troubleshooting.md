---
id: troubleshooting
title: Troubleshooting
sidebar_label: Troubleshooting
---


Whenever an installation fails, it is highly likely there is a misconfiguration, a network interruption, or invalid library bundles were used. **nuts** provides robust mechanisms to circumvent these issues, ranging from targeted diagnostics to various levels of workspace reinitialization.

## Common Error Messages

If you encounter issues during execution, check these common scenarios:

### "Artifact not found"
* **Cause**: The artifact ID is incorrect, or the required repository is not configured.
* **Solution**: Verify the `groupId:artifactId#version` syntax. Check your configured repositories using `nuts settings list repos` to ensure the source repository is available.

### "Java version incompatible"
* **Cause**: The application requires a different JDK version than the one currently active.
* **Solution**: You can instruct **nuts** to auto-provision the correct version by using `nuts exec --java-version=<required-version> <app>`. Alternatively, manually provision it with `nuts settings add java --download --jdk --version=<version>`.

### "Permission denied"
* **Cause**: The active user lacks write access to the workspace directories.
* **Solution**: Verify ownership of the workspace directory (usually `~/.nuts` or XDG equivalents). Never mix `sudo` and standard user execution within the same workspace, as this causes file ownership conflicts.

### "Repository unreachable"
* **Cause**: Network connectivity issues, corporate firewalls, or proxy misconfigurations.
* **Solution**: Check your internet connection. If you are behind a corporate proxy, configure the HTTP proxy settings in your environment variables or directly within the **nuts** configuration.

## Diagnostic Commands

Before attempting to reset your workspace, use these commands to gather diagnostic information:

* `nuts info` — Displays deep metadata about the active workspace environment.
* `nuts version` — Shows version information for the bootstrap, runtime, and Java environment.
* `nuts settings list repos` — Lists all configured repositories and their status.
* `nuts search --installed` — Lists all artifacts currently installed in the workspace.

## Workspace Recovery Modes

If diagnostic commands do not resolve the issue, you can use the built-in recovery and reset modes to repair the workspace.

### Recover Mode

**Recover mode** applies best efforts to correct configurations without losing them. It deletes all cached data and downloaded libraries, forcing them to be re-downloaded, and searches for valid **nuts** installation binaries to run (acting as a forced update). 

To run **nuts** in recover mode, type:

```bash
nuts -z
```

### Newer Mode

**Newer mode** applies best efforts to reload cached files and libraries from disk, useful if the cache has fallen out of sync.

To run **nuts** in newer mode, type:

```bash
nuts -N
```

### Reset Mode

**Reset mode** applies all efforts to correct configuration by actually **deleting** it (and all workspace files) to create a fresh workspace. This is a radical action. Do not invoke this unless you understand the consequences.

To run **nuts** in reset mode, type:

```bash
nuts -Z
```

### Hard-Reset Mode

**Hard-reset mode** deletes all **nuts** configuration files across **all** workspaces on the system. This is an extreme action. Do not invoke this unless you understand the consequences.

To run **nuts** in hard-reset mode, type:

```bash
nuts --hard-reset
```

### Kill Mode

**Kill mode** is a special variant of reset mode where the workspace will **not** be recreated after deletion. This effectively uninstalls the workspace. It is achieved by combining reset mode with the `--skip-boot` (`-Q`) option.

To run **nuts** in kill mode, type:

```bash
nuts -ZQ
```

You can run hard-reset in kill mode too, which removes **nuts** entirely from the system:

```bash
nuts --hard-reset
```

### Recovery Mode Summary

| Mode       | Flag               | What it does                                                 | When to use                           |
|------------|--------------------|--------------------------------------------------------------|---------------------------------------|
| Recover    | `-z`               | Deletes corrupted cache, forces dependency re-download       | App won't start, suspect bad download |
| Newer      | `-N`               | Reloads cached files from disk                               | Cache out of sync with disk           |
| Reset      | `-Z` or `--reset`  | **Deletes workspace config and apps**, keeps global settings | Workspace is broken beyond repair     |
| Hard Reset | `--hard-reset`     | **Deletes ALL workspaces and global config**                 | Complete uninstall/reinstall          |
| Kill       | `-ZQ`              | Deletes workspace, does not recreate it                      | Removing Nuts entirely                |

## After Invoking Reset Mode

After invoking a reset mode (`-Z` or `--hard-reset`), the **nuts** shell launchers (installed by `nuts settings`) will not be available anymore. The `PATH` environment variable will temporarily point to a non-existing folder. 

You must use the JAR-based invocation at least once to reinstall these commands and restore your environment:

```bash
java -jar nuts.jar
```
