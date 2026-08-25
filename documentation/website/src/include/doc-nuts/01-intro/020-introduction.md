---
id: introduction
title: Java Package Manager
sidebar_label: Introduction
---

**nuts** (Network Updatable Things Services) is a portable runtime package manager for the JVM. It seamlessly handles remote artifacts, installs them locally, and executes them on demand, solving the "fat JAR" problem by deferring dependency resolution to execution time. 

By utilizing standard Maven POM descriptors, **nuts** is completely transparent to your existing build process while drastically simplifying the way Java applications are packaged and distributed. 

## Core Concepts

* **Artifacts**: A package or application. In **nuts**, any standard Maven coordinate (`groupId:artifactId#version`) is a valid artifact. By design, **nuts** supports multiple artifact formats beyond just JARs and WARs, allowing it to adapt to the target OS and architecture.
* **Repositories**: Storage locations for artifacts. A repository can be local (for your machine) or remote (such as Maven Central). **nuts** can also proxy remote repositories to cache artifacts locally, conserving network bandwidth.
* **Workspaces**: Isolated environments managing a set of repositories and configurations, much like `virtualenv` in Python. Workspaces keep your global environment clean and allow you to isolate dependencies on a per-project basis.
* **Descriptors**: Metadata defining an artifact's dependencies. **nuts** natively understands Maven POMs and gracefully resolves dependencies over the network when an artifact is executed.

## How It Works

**nuts** bridges the gap between build tools and OS-level package managers. It acts similarly to **npm** or **pip** but is tailored for the Java ecosystem. 

1. **Install**: When you request an application, **nuts** locates the artifact in your repositories and stages it.
2. **Resolve**: At runtime, **nuts** parses the artifact's descriptor, downloading any missing dependencies to the local cache.
3. **Execute**: It configures the classpath, provisions the necessary JDK if missing, and launches the application.

Because resolution is dynamic, you can install and seamlessly switch between multiple versions of the exact same artifact. 

## Commands Overview

**nuts** provides a rich CLI with intuitive commands. Here are the most common verbs:

| Command | Description |
|---------|-------------|
| `exec` | Execute an artifact or a system command. |
| `which` | Detect the proper artifact or system command path to execute. |
| `install` / `uninstall` | Install or remove an artifact using its deployed installer. |
| `update` / `check-updates` | Search the repository for newer versions of installed artifacts. |
| `deploy` / `undeploy` | Manage artifacts (and artifact installers) within local repositories. |
| `fetch` / `push` | Download artifacts from, or upload artifacts to, remote repositories. |
| `search` | Query and discover existing or installable artifacts. |
| `welcome` | Bootstraps the environment and displays a welcome message. |
