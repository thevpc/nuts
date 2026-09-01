---
id: overview
title: Overview
---

**nuts** (Network Updatable Things Services) is a portable runtime package manager for Java and the JVM. Think of **nuts** as the Java equivalent to Python's **`uv`** (or Node's **`npx`**): an all-in-one runtime package manager that transparently provisions JDKs, manages isolated workspaces, and executes any Maven artifact instantly on demand, side-by-side with other versions, without the bloat of fat JARs.

If you publish to Maven Central, your application is already a **nuts** package—no special registry or packaging format required.

## The Problem

For years, Java has lacked a true runtime package manager. To distribute an application, developers typically build "fat JARs" (shadow JARs) containing all dependencies. This approach has significant drawbacks:

* **Bloat**: Bundling dependencies inflates file sizes, wasting disk space and network bandwidth.
* **No side-by-side versioning**: Running multiple versions of the same tool locally often involves brittle scripts or environment variable juggling.
* **Complex runtime setup**: Users must manually ensure the correct JDK version is installed before they can run a Java application.
* **Poor CLI experience**: There is no simple `install and run` workflow native to the ecosystem.

## The Solution

**nuts** shifts dependency resolution from build-time to runtime. It natively understands standard Maven POM descriptors, dynamically fetching and resolving required dependencies precisely when an application is executed.

```bash
nuts install org.example:myapp
nuts myapp
```

## Core Concepts

* **Artifacts**: A package or application. In **nuts**, any standard Maven coordinate (`groupId:artifactId#version`) is a valid artifact. Beyond JARs and WARs, **nuts** supports multiple artifact formats adapted to target operating systems.
* **Repositories**: Storage locations for artifacts. A repository can be local or remote (such as Maven Central). **nuts** can proxy remote repositories to cache artifacts locally.
* **Workspaces**: Isolated environments managing a set of repositories and configurations, much like Python's `uv` workspaces or `virtualenv`. Workspaces isolate dependencies on a per-project basis.
* **Descriptors**: Metadata defining an artifact's dependencies. **nuts** parses descriptors at runtime and gracefully resolves dependencies over the network.

## Key Differentiators

* **Zero External Dependencies**: The **nuts** bootstrap JAR is entirely self-contained.
* **Cross-Platform**: Operates with identical behavior across Linux, macOS, and Windows.
* **Workspace Isolation**: Supports isolated environments, keeping your global setup clean.
* **JDK Auto-Provisioning**: Automatically detects, downloads, and configures the appropriate JDK version (and vendor) required by the application.
* **Structured Output**: Built-in support for generating output in JSON, YAML, XML, TSON, and plain text formats.
* **Self-Hosting**: **nuts** manages itself—using the same mechanisms to install, update, and uninstall its own binaries.

## What Nuts is NOT

* **Not a build tool**: You still use Maven, Gradle, or Ant to compile and package your code. **nuts** is used to install, update, and run the resulting artifacts.
* **Not a replacement for Maven Central**: **nuts** seamlessly consumes artifacts from Maven Central and standard repositories rather than replacing them.

## Command-Line Verbs Overview

**nuts** provides a rich CLI with intuitive command verbs:

| Command Verb | Description |
|---|---|
| `exec` | Execute an artifact or a system command. |
| `which` | Detect the proper artifact or system command path to execute. |
| `install` / `uninstall` | Install or remove an artifact using its deployed installer. |
| `update` / `check-updates` | Search the repository for newer versions of installed artifacts. |
| `deploy` / `undeploy` | Manage artifacts (and artifact installers) within local repositories. |
| `fetch` / `push` | Download artifacts from, or upload artifacts to, remote repositories. |
| `search` | Query and discover existing or installable artifacts. |
| `welcome` | Bootstraps the environment and displays a welcome message. |

## Nuts Application Framework (NAF)

**nuts** is built upon the [Nuts Application Framework (NAF)](/doc-naf.html), a robust foundation for building feature-rich Java applications. If you are developing applications that deeply integrate with **nuts**, refer to the NAF documentation.
