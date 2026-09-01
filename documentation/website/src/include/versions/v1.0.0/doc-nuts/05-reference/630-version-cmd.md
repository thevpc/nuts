---
id: version-cmd
title: Version
---

## Synopsis

```sh
nuts version
```

## Description

The `version` command displays the current version of the **nuts** runtime.

**nuts** uses a decoupled architecture consisting of a bootstrap API and a runtime implementation. 
* The API (the `nuts-*.jar` file, ~500KB) contains only the minimum logic required to bootstrap the environment and fetch the runtime.
* The Implementation (the full runtime, ~3MB) contains the actual logic for dependency resolution, package management, and execution.

Generally, the implementation version tracks the API version, but they can evolve independently.

```bash
$ nuts version
{{apiVersion}}/{{runtimeVersion}}
```

Here, the `version` command displays both the API version (`{{apiVersion}}`) and the implementation version.
