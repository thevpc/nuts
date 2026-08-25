---
id: update-cmd
title: Update
---

## Synopsis

```sh
nuts update [options] [<artifact-id>]
```

## Description

The `update` command resolves available updates for the specified artifacts and applies them to the workspace. If no artifact is specified, it checks for and updates all currently installed artifacts.

Unlike `check-updates`, which only reports available newer versions, the `update` command automatically fetches and installs the latest stable versions. It can also be used to update the **nuts** package manager itself.

## Options

* `-a`, `--all`: Update all applicable artifacts.
* `-w`, `--workspace`: Update workspace dependencies.
* `-i`, `--installed`: Update explicitly installed artifacts.
* `-e`, `--extensions`: Update nuts extensions.
* `-c`, `--companions`: Update nuts companions.
* `-r`, `--runtime`: Update the nuts runtime implementation.
* `-A`, `--api`: Update the nuts API bootstrap jar.
* `-v <version>`, `--to-version <version>`: Force an update to a specific version (this allows downgrading as well).
* `-y`, `--yes`: Automatically confirms all update prompts.
* `--dry`: Simulates the update process, showing what would be updated without applying changes.
* `--json`: Formats the update results as structured JSON.

## Examples

Update all installed artifacts in the workspace:
```bash
nuts update
```

Update a specific artifact:
```bash
nuts update netbeans-launcher
```

Force an artifact to a specific version (can be used to downgrade):
```bash
nuts update netbeans-launcher --to-version 1.2.0
```

Update the **nuts** package manager runtime and API:
```bash
nuts update -r -A
```

Simulate an update to see what would change:
```bash
nuts update --dry
```

## Related Commands

* `check-updates`: Only checks for updates without applying them.
* `install`: Installs specific versions of an artifact.
* `reinstall`: Reinstalls the current version of an artifact to repair it.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/update.ntf")}}
``````
