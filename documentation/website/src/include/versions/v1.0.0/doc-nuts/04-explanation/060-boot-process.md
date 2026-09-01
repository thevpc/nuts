---
id: bootProcess
title: Bootstrap Process & Architecture
sidebar_label: Boot Process
---

# Bootstrap Process & Architecture

At the core of **nuts** lies an intelligent, zero-dependency bootstrap mechanism implemented by `nuts-boot` (specifically orchestrated by `NBootWorkspaceImpl`). The bootstrap layer is responsible for turning a minimal executable JAR (~500KB) into a fully functional, modular runtime environment by resolving, caching, verifying, and launching the appropriate `nuts-api` and `nuts-runtime` components.

```text
┌──────────────────────────────────────────────────────────────────┐
│                         CLI / Launcher                           │
│                 java -jar nuts.jar [options] [args]              │
└─────────────────────────────────┬────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                     nuts-boot (NBootWorkspace)                   │
│  ┌────────────────────────┐        ┌──────────────────────────┐  │
│  │ 1. Parse Args & Config │ ────►  │ 2. Resolve Workspace     │  │
│  └────────────────────────┘        └─────────────┬────────────┘  │
│                                                  │               │
│  ┌────────────────────────┐        ┌─────────────▼────────────┐  │
│  │ 4. Resolve Runtime     │ ◄────  │ 3. Resolve API Version   │  │
│  └───────────┬────────────┘        └──────────────────────────┘  │
│              │                                                   │
│  ┌───────────▼────────────┐        ┌──────────────────────────┐  │
│  │ 5. Build ClassLoader   │ ────►  │ 6. Factory SPI Discovery │  │
│  └────────────────────────┘        └─────────────┬────────────┘  │
└──────────────────────────────────────────────────┼───────────────┘
                                                   │
                                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                    nuts-runtime (NWorkspace)                     │
│                  Full Package Management Engine                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Core Design Principles

1. **Zero External Dependencies**: The bootstrap JAR embeds its own lightweight CLI argument tokenizer (`NBootCmdLine`), JSON parser (`NBootJsonParser`), Maven coordinate solver (`NReservedMavenUtilsBoot`), and repository communication layer. It requires only a standard Java Virtual Machine (Java 8+).
2. **Three-Tier Modularity**: `nuts-boot`, `nuts-api`, and `nuts-runtime` are strictly decoupled and independently versioned. The bootstrapper can dynamically load any compatible API/runtime version without modifying the bootstrap JAR itself.
3. **Local-First Caching**: Boot assets (the runtime JAR and its direct dependencies) are cached under the workspace's `LIB` store (`<workspace-lib>/id/net/thevpc/nuts/nuts-runtime/<version>/`). Startup is instantaneous once cached, requiring no network round-trips.
4. **Self-Healing & Fallbacks**: If binary incompatibilities (`IncompatibleClassChangeError`, `LinkageError`) or corrupted cache entries occur, the bootstrap layer automatically invalidates cache descriptors and retries resolution.

---

Nuts uses a three-tier bootstrap: nuts-boot (~500KB) resolves the best nuts-api and nuts-runtime versions from Maven repositories, constructs an isolated classloader, and hands off to the runtime. If cached binaries are corrupted, the bootstrap automatically invalidates the cache and retries.

For the full step-by-step sequence, classloader hierarchy, and recovery mechanisms, see the [Bootstrap Deep Dive](#bootstrap-deep-dive).

