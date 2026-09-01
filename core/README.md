# Nuts Core Module

This directory contains the essential foundation of the Nuts Package Manager.

## Submodule Architecture

| Submodule | Type | Description | Key Dependencies |
| :--- | :--- | :--- | :--- |
| **`nuts-boot`** | Library | Workspace bootstrapper library. Responsible for downloading, creating, and initializing Nuts workspaces. | None (Zero external dependencies) |
| **`nuts-api`** | Library | Core API interfaces and Service Provider Interfaces (SPI). | `nuts-boot` |
| **`nuts-lib`** | Library | Core Library wrapper for embedding full Nuts capabilities as a Java library. | `nuts-api` |
| **`nuts-runtime`** | Implementation | Execution engine loaded dynamically at runtime by `nuts-boot` and linked to `nuts-api`. | `nuts-api` |
| **`nuts-app`** | Application | Lightweight CLI executable launcher. Downloads required classes on the fly. | `nuts-boot` |
| **`nuts-app-full`** | Application | Standalone fat binary with pre-packaged runtime dependencies. | Self-contained |

---

## How to Build & Test Core

Build all core modules from root:
```bash
mvn clean install -pl core
```

Run tests for a specific core submodule:
```bash
mvn test -pl core/nuts-runtime
```
