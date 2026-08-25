---
id: deploy-cmd
title: Deploy
---

## Synopsis

```sh
nuts deploy [options] <file-or-artifact>
```

## Description

The `deploy` command installs a local artifact (such as a locally built JAR file or a Maven POM descriptor) into a local **nuts** repository. Once deployed, the artifact becomes available for installation and execution within your workspace, just as if it had been fetched from a remote repository.

This is essential for local development, allowing you to test artifacts before pushing them to remote servers.

## Options

* `--repository=<repo>`: Specifies the target local repository to deploy to.
* `-y`, `--yes`: Skip confirmation prompts.

## Examples

Deploy a locally built JAR file:
```bash
nuts deploy ./target/my-app-1.0.jar
```

Deploy using a specific descriptor:
```bash
nuts deploy ./pom.xml
```

## Related Commands

* `push`: Synchronizes a local repository to a remote repository.
* `undeploy`: Removes a deployed artifact from a local repository.

``````ntf
{{include($"$root/core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/command/deploy.ntf")}}
``````
