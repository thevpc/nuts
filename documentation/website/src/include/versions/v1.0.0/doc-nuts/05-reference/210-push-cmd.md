---
id: push-cmd
title: Push
---

## Synopsis

```sh
nuts push [options] <artifact-id>
```

## Description

The `push` command uploads an artifact from a local repository to a remote repository. This essentially synchronizes the local artifact deployment with its remote peer, making the artifact available to other users or environments.

It acts as the counterpart to `fetch`, allowing developers to publish their completed applications or libraries.

## Options

* `--repository=<repo>`: The target remote repository to push the artifact to.
* `-y`, `--yes`: Suppress confirmation prompts.

## Examples

Push an artifact to the default remote repository:
```bash
nuts push net.thevpc.app:my-awesome-tool#1.0.0
```

## Related Commands

* `deploy`: Deploys an artifact locally.
* `fetch`: Downloads an artifact from a remote repository.
