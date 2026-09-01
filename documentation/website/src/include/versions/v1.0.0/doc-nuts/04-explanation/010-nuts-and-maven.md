---
id: nutsAndMaven
title: Nuts and Maven
sidebar_label: Nuts and Maven
---

**nuts** is not a build tool. It is a runtime package manager that consumes Maven artifacts. It serves the role that `npm` or `pip` play in the JavaScript and Python ecosystems, but tailored specifically for Java.

| Feature | Nuts | Maven | SDKMAN | jbang | Homebrew |
|---|---|---|---|---|---|
| Installs apps | ✅ | ❌ | ⚠️ (JDKs only) | ⚠️ (scripts) | ✅ |
| Uses Maven metadata | ✅ | ✅ | ❌ | ✅ | ❌ |
| Side-by-side versions | ✅ | ❌ | ✅ | ❌ | ❌ |
| Zero dependencies | ✅ | ❌ | ❌ | ❌ | ❌ |
| Cross-platform | ✅ | ✅ | ⚠️ | ✅ | ❌ (macOS/Linux only) |
| JDK provisioning | ✅ | ❌ | ✅ | ❌ | ❌ |
| Fat JAR overhead | ❌ (Shared libs) | ✅ (Fat JARs) | N/A | ❌ | N/A |
| Dependency deduplication| ✅ (System-wide)| ❌ | N/A | N/A | N/A |

## Build Time vs Runtime

**Maven** is designed for the build phase. When building an application for distribution without a package manager, developers typically resort to creating "Fat JARs" (or Uber JARs) — a single massive archive containing the application code and every single dependency. This results in bloated binaries, slow downloads, and wasted disk space when multiple applications share the same libraries.

**nuts** resolves dependencies at runtime or install time. When you distribute an application with **nuts**, you only publish the thin application JAR (often just a few kilobytes). When the user installs or runs the application, **nuts** reads the `pom.xml`, computes the dependency tree, and downloads the required libraries into a shared local cache. If ten different applications depend on `commons-lang3`, the library is downloaded and stored only once.

## Complementary Tools

Maven and Nuts are not competitors; they are highly complementary tools that form a complete CI/CD pipeline:

- 1. **Build Phase (Maven/Gradle)**: Developers use Maven or Gradle to compile source code, run unit tests, and package the thin artifact.
- 2. **Publish Phase (Maven/Gradle)**: The thin artifact and its `pom.xml` are deployed to Maven Central or a private repository (e.g., Nexus, Artifactory).
- 3. **Distribution Phase (Nuts)**: On the target system (servers, developer machines, CI agents), users execute `nuts install my.group:my-app` to resolve, download, and launch the application.

## When to Use What

- **Use Maven or Gradle** when you are writing code, compiling Java files, running tests, or generating documentation. They are build lifecycle managers.
- **Use Nuts** when you want to install an application, run a command-line tool, manage multiple versions of a Java application side-by-side, or automate deployment scripts on target servers.

## Technical Relationship

**nuts** natively understands Maven repositories and the `pom.xml` descriptor format. It communicates directly with Maven Central to resolve coordinates, parse dependency scopes, and handle version conflict resolution. 

Importantly, **nuts does NOT require Maven to be installed** on the system. The **nuts** runtime contains its own highly optimized, lightweight POM solver and repository client.
