---
id: check-updates-cmd
title: Check Updates
---

## Synopsis

```sh
nuts check-updates [options] [<artifact-id>]
```

## Description

The `check-updates` command resolves and displays available updates for the specified artifact, or for all installed artifacts if no specific artifact is provided. 

**This command does not perform the updates.** It is used strictly for information and verification purposes, allowing you to see what new versions are available before deciding to upgrade. To actually apply the updates, use the `update` command.

## When to Use

* Auditing the current workspace for outdated packages.
* Checking if a critical security patch is available for a specific artifact.
* Scripting environments where you want to evaluate updates before triggering an upgrade cycle.

## Options

* `-a`, `--all`: Checks for updates across all packages.
* `-i`, `--installed`: Checks for updates only for explicitly installed artifacts.
* `-e`, `--extensions`: Checks for updates for installed nuts extensions.
* `-c`, `--companions`: Checks for updates for nuts companions.
* `-r`, `--runtime`: Checks for updates for the nuts runtime.
* `-A`, `--api`: Checks for updates for the nuts API.
* `--json`: Outputs the available updates in a structured JSON format, useful for CI/CD parsing.
* `--table`: Displays the updates in a tabular format.
* `--dry`: Simulates the check.

## Examples

Check for updates across all installed artifacts:
```bash
nuts check-updates
```

Check for updates for a specific artifact:
```bash
nuts check-updates netbeans-launcher
```

Check for updates for the nuts API and runtime:
```bash
nuts check-updates -A -r
```

## Related Commands

* `update`: Checks for and applies available updates.
* `search`: Searches for specific versions of artifacts.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/check-updates.ntf")}}
``````
