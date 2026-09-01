---
id: undeploy-cmd
title: Undeploy
---

## Synopsis

```sh
nuts undeploy [options] <artifact-id>
```

## Description

The `undeploy` command removes a deployed artifact from a local repository. 

This command is the reverse of `deploy` and is typically used to clean up local development repositories or remove testing artifacts that are no longer needed. Note that this removes the artifact from the repository index and storage, making it unavailable for new installations.

## Options

* `--repository=<repo>`: Specifies which local repository to undeploy from.
* `-y`, `--yes`: Skip confirmation prompts.

## Examples

Undeploy a specific artifact version:
```bash
nuts undeploy net.thevpc.app:my-awesome-tool#1.0.0
```

## Related Commands

* `deploy`: Deploys a local artifact to a repository.
* `uninstall`: Removes an installed artifact from the active workspace.


