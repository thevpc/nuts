---
title: Architecture & Design
---

# Architecture & Design

**nuts** (Network Updatable Things Services) is built on a modular, secure, and extensible architecture. Its primary goal is to shift dependency resolution from build-time to runtime, enabling highly efficient, multi-version package management for the Java ecosystem.

## Design Constraints

The design of **nuts** is driven by three core constraints:

### 1. Zero External Dependencies
The **nuts** bootstrap JAR is designed to be completely self-contained. At approximately 500KB, it includes its own minimal CLI parser, expression engine, text formatter, and secure network client. No external libraries or tools (not even Maven or curl) are required to start the engine, download dependencies, and boot the runtime. This zero-dependency footprint guarantees a pristine installation and bootstrapping process across any environment.

### 2. Maven-Native Package Format
**nuts** does not invent a new packaging format. Every existing Maven artifact with a valid `pom.xml` descriptor is automatically a valid **nuts** package. The system uses standard Maven coordinates (`groupId:artifactId#version`) for artifact resolution. By directly consuming Maven Central and private repositories, **nuts** instantly taps into the world's largest ecosystem of Java libraries and applications.

### 3. Cross-Platform Consistency
**nuts** guarantees identical behavior and feature sets across Linux, macOS, and Windows. This includes consistent CLI semantics, XDG-compliant file system layouts, shell integration, and OS-level desktop launchers. The goal is to provide a reliable, predictable developer and deployment experience, eliminating platform-specific edge cases in installation scripts or CI/CD pipelines.

## Three-Tier Architecture

To achieve extreme modularity and version flexibility, **nuts** is structured into three distinct tiers — each independently versioned and resolvable at runtime:

```text
┌─────────────────────────────────────────────┐
│        Applications & Companion Tools        │
│    (CLI tools, Desktop apps, nsh, nmvn)       │
├─────────────────────────────────────────────┤
│            nuts-runtime (Engine)              │
│  ~3MB · POM Solver · Repo Manager · NTF      │
├─────────────────────────────────────────────┤
│         nuts-api (Contract Layer)             │
│     Interfaces · SPIs · Data Model            │
├─────────────────────────────────────────────┤
│         nuts-boot (Bootstrap Layer)           │
│ ~500KB · CLI Parser · Version Resolver · Boot │
└─────────────────────────────────────────────┘
```

- 1. **nuts-boot (Bootstrap Layer)**: The entry point — the JAR you download and run with `java -jar nuts.jar`. It contains a minimal CLI parser, Maven repository client, and version resolver. Its sole job is to locate the *best* `nuts-api` version, then find the *best* `nuts-runtime` compatible with that API. It has zero dependencies and can bootstrap the entire system from scratch.
- 2. **nuts-api (Contract Layer)**: Defines the public interfaces, SPIs, and data model that all **nuts** components program against. Applications and companion tools depend only on this layer. By keeping the API separate, the runtime implementation can evolve independently without breaking applications.
- 3. **nuts-runtime (Engine)**: The full implementation of all SPIs defined in `nuts-api`. It provides the Maven POM dependency solver, repository manager, security enforcement, process execution engine, and advanced terminal formatting (NTF). This tier is loaded dynamically by `nuts-boot` — it is never bundled statically.
- 4. **Applications**: The top layer consisting of companion tools (like `nsh`, `nmvn`) and user applications installed via **nuts**. They consume the `nuts-api` interfaces to interact with the workspace.

## Bootstrap Process

The bootstrap sequence, implemented in `NBootWorkspaceImpl`, is the critical path that turns a single JAR into a fully operational package manager. When you run `java -jar nuts.jar` or use a native launcher:

### Step 1 — Parse & Configure
The boot JAR parses command-line arguments, reads inherited system properties (`nuts.boot.args`, `nuts.args`), detects the current OS, architecture, and shell environment, and resolves the workspace location (default, named, or path-based).

### Step 2 — Resolve the Best API Version

The boot JAR determines which `nuts-api` version to load. "Best" here means **the latest available version**, determined by comparing version numbers from the Maven `maven-metadata.xml` across all configured repositories:

- If a specific version is pinned (via workspace config or CLI), that exact version is used.
- If the version is set to `LATEST` or `RELEASE`, the boot JAR queries all boot repositories, parses their `maven-metadata.xml` files, and selects the **highest version number** found.
- If no version is specified at all (blank), the boot JAR falls back to its own embedded version — `NUTS_BOOT_VERSION` — which is the API version it was compiled against.
- If the session is inherited (embedded mode), the boot JAR always uses its own `NUTS_BOOT_VERSION` to avoid version conflicts within the same JVM.

This means the boot JAR can bootstrap a **different (newer or older) API version** than the one it was built with — the boot layer and the API layer are independently versioned.

### Step 3 — Resolve the Best Runtime

With the API version determined (say `0.8.4`), the boot JAR searches for the **best compatible `nuts-runtime`**. The compatibility rule is a **version prefix match**: only runtime versions that start with the API version followed by a dot are considered. For example, if the API is `0.8.4`, the runtime must be `0.8.4.x` (e.g., `0.8.4.0`, `0.8.4.1`, `0.8.4.5`). Among all matching versions, the **highest version number wins**.

The resolution follows a **local-first strategy**:

- 1. **Local workspace lib cache** — checked first for fast startup without network access.
- 2. **Remote repositories** (Maven Central, configured repos) — queried only if no valid local version is found (or if the fetch strategy is `ANYWHERE`).
- 3. **Fallback cache** — if both local and remote fail, the boot JAR scans the workspace's lib directory for any previously downloaded runtime JAR with a matching version prefix.

Once the runtime artifact is identified, its dependency tree is resolved from the `.nuts` descriptor (a lightweight JSON format) or the standard Maven `pom.xml`.

### Step 4 — Build the Classworld
The bootstrapper downloads any missing JARs (the runtime and its transitive dependencies) and constructs an isolated `NBootClassLoader` — a custom classloader hierarchy that keeps the runtime's classpath fully isolated from the boot layer and from the application's own dependencies.

### Step 5 — Discover the Workspace Factory
Using Java's `ServiceLoader` mechanism on the newly constructed classloader, `nuts-boot` discovers implementations of the `NBootWorkspaceFactory` SPI. The factories are sorted by priority, and the best one is used to create the workspace instance.

### Step 6 — Handoff
The workspace factory creates a `NWorkspaceBase` instance, which takes full control. From this point on, `nuts-boot` is no longer involved — the runtime handles all CLI commands, dependency resolution, installation, execution, and configuration.

### Recovery & Resilience
If the loaded runtime binaries are incompatible (e.g., due to a corrupted cache or a version mismatch causing `IncompatibleClassChangeError`), the bootstrap automatically detects this, expires the cache, and retries the entire resolution from Step 2 with fresh downloads — without requiring user intervention.

## Self-Hosting and Updating

**nuts** uses its own package management engine to manage itself. Because `nuts-boot` dynamically resolves both the API and runtime versions, the update process is elegant:

```bash
nuts update
```

This resolves newer versions of the API and runtime, downloads them, updates the workspace configuration, and on the next launch, `nuts-boot` will load the new versions automatically. The boot JAR itself rarely needs updating — it delegates all real work to the runtime it resolves.

## Extension Model

**nuts** provides a powerful Service Provider Interface (SPI) extension model. Developers can seamlessly inject custom capabilities into the runtime without modifying the core system. Extensions can provide:

- **Custom Repositories**: Integration with specialized artifact stores (e.g., S3, Git, internal HTTP endpoints).
- **Custom Executors**: Support for executing non-Java artifacts (e.g., Python scripts, native binaries, shell scripts).
- **Custom Descriptors**: Parsers for metadata formats other than standard `pom.xml`.

Extensions are discovered via `ServiceLoader` and loaded into the workspace's classloader, maintaining full isolation from other extensions and the core runtime.

## Isolation Levels

**nuts** supports multiple isolation levels for different use cases:

| Level | Behavior |
|-------|----------|
| **System** (default) | Standard workspace, full persistence, shared config |
| **User** | Per-user isolation |
| **Confined** | Restricted operations |
| **Sandbox** | Temporary workspace in a temp directory, standalone strategy forced, deleted on exit |
| **Memory** | In-memory workspace, no disk persistence |

Sandbox and Memory modes are particularly useful for CI/CD pipelines and automated testing where no state should persist between runs.

## Nuts Text Format (NTF)

To ensure high-quality CLI output, **nuts** includes its own terminal rendering engine known as the Nuts Text Format (NTF). NTF provides a robust, cross-platform markup language for:

- ANSI colorization and syntax highlighting.
- Responsive table drawing with ASCII and Unicode borders.
- Tree structures for dependency graphs.
- Structured output rendering tailored to terminal widths.

NTF gracefully falls back to plain text if a non-interactive console is detected, ensuring logs and piped data remain clean and parseable.