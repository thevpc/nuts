---
id: fetch-cmd
title: Fetch
---

## Synopsis

```sh
nuts fetch [options] <artifact-id>
```

## Description

The `fetch` command downloads and caches an artifact and its dependencies into the local machine's cache without installing or executing it. Once downloaded, the artifact transitions to the `fetched` status.

This command is particularly useful when you need to prepare an environment for offline use, verify that an artifact is available in remote repositories, or simply warm up the workspace cache before a deployment script runs.

## Options

The `fetch` command supports standard global options, including:
* `--repository=<repo>`: Fetch exclusively from the specified repository.
* `-y`, `--yes`: Automatically confirm any resolution prompts.
* `--dry`: Perform a dry run to check what would be fetched without actually downloading anything.

## Examples

Fetch a specific version of an artifact:
```bash
nuts fetch net.thevpc.app:netbeans-launcher#1.2.2
```

Fetch the latest available version:
```bash
nuts fetch net.thevpc.app:netbeans-launcher
```

Verify availability using a dry run:
```bash
nuts fetch --dry net.thevpc.app:netbeans-launcher
```

## Related Commands

* `install`: Installs an artifact after fetching it.
* `deploy`: Deploys a local artifact to a repository.
* `search`: Searches for artifacts without downloading them.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/fetch.ntf")}}
``````
