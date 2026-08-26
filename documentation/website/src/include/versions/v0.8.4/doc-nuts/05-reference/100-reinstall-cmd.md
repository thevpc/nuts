---
id: reinstall-cmd
title: Reinstall
---

## Synopsis

```sh
nuts reinstall [options] <artifact-id>
```

## Description

The `reinstall` command is used to reinstall an existing, already installed artifact. 

When you execute this command, **nuts** forces the re-resolution of the artifact's dependencies, repairs corrupted installation states, and re-executes the installer script. This is highly useful when an artifact's execution environment has been compromised, when files have been accidentally deleted from the workspace, or when dependencies need to be force-synced.

## Options

* `-y`, `--yes`: Automatically confirm prompts.
* `--dry`: Perform a dry run simulation.

## Examples

Reinstall a specific artifact to repair it:
```bash
nuts reinstall net.thevpc.app:netbeans-launcher
```

## Related Commands

* `install`: Installs a new artifact.
* `uninstall`: Removes an artifact.
* `update`: Upgrades an artifact to a newer version.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/install.ntf")}}
``````
