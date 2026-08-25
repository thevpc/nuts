---
id: search-cmd
title: Search
---

## Synopsis

```sh
nuts search [options] <query>
```

## Description

The `search` command queries available repositories to find artifacts.

Artifacts can be in several states:
* `unavailable`: No registered repository can serve the artifact.
* `available`: At least one repository can serve the artifact.
* `fetched`: The artifact is cached locally.
* `installed`: The artifact is fetched and fully installed in the workspace.
* `installed default`: The artifact is installed and marked as the default version.

### Artifact Queries

An artifact query is a generalized artifact ID that supports wildcards and version intervals.

Examples of artifact queries:
```bash
# All artifacts that start with netbeans, regardless of groupId
nuts search "netbeans*"
nuts search "*:netbeans*"

# All artifacts in the net.thevpc.app groupId
nuts search "net.thevpc.app:*"

# All artifacts in any net.thevpc.* sub-group
nuts search "net.thevpc.*:*"

# Artifacts filtered by OS and architecture
nuts search "netbeans*?os=windows&arch=x86_64"

# Version greater than 1.2.0 (excluding 1.2.0)
nuts search "netbeans-launcher#]1.2.0,["

# Version greater than or equal to 1.2.0
nuts search "netbeans-launcher#[1.2.0,["
```

## Options

You can filter search results by status:
* `-i`, `--installed`: Search only for installed artifacts.
* `--local`: Search only for locally fetched/cached artifacts.
* `--remote`: Search only for non-fetched, remote artifacts.
* `--online`: Search in installed, then local, then remote. Stops when the first match is found.
* `-a`, `--anywhere`: Search everywhere and return all results.

### Output Formatting

The `search` command supports structured output, making it highly useful for scripting. Use the `-l` or `--long` flag for verbose listings.

```bash
$ nuts search -i -l
I-X 2019-08-26 09:53:53.141 anonymous vpc-public-maven net.thevpc.app:netbeans-launcher#1.2.1
IcX 2019-08-24 11:05:49.591 admin     maven-local      net.thevpc.app.nuts.toolbox:nsh#
I-x 2019-08-26 09:50:03.423 anonymous vpc-public-maven net.thevpc.app:kifkif#1.3.3
```

You can change the output format using global flags: `--plain`, `--json`, `--xml`, `--table`, or `--tree`.

```bash
$ nuts search -i -l --json
```
```json
[
  {
    "id": "vpc-public-maven://net.thevpc.app:netbeans-launcher#1.2.1",
    "descriptor": {
      "id": "net.thevpc.app:netbeans-launcher#1.2.1",
      "packaging": "jar",
      "executable": true
    }
  }
]
```

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/search.ntf")}}
``````
