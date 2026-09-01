---
id: bootstrap-deep-dive
title: Bootstrap Deep Dive
sidebar_label: Bootstrap Deep Dive
---

# Bootstrap Deep Dive

This technical specification details the complete sequence of operations performed by `NBootWorkspaceImpl` during the **nuts** bootstrap lifecycle.

## The Bootstrap Lifecycle Step-by-Step

When you execute `nuts` (or `java -jar nuts.jar`), `NBootWorkspaceImpl` executes the following sequence:

### Step 1: Environment and Option Parsing

1. **Inherited System Properties**: Reads options from `nuts.boot.args` and `nuts.args` system properties (unless `--skip-inherited` is specified).
2. **Command Line Arguments**: Tokenizes CLI options using `NBootWorkspaceCmdLineParser`.
3. **Custom Boot Options**:
   - `---m2`: Enables or disables local Maven repository (`~/.m2/repository`) integration.
   - `---connection-timeout`: Sets network timeout thresholds for remote repository checks.
4. **Bot / Automation Mode**: If launched for shell completion or with `--bot`, interactive prompts are disabled (`confirm=ERROR` or `confirm=NO`).

### Step 2: Workspace Location & Layout Resolution

The bootstrapper determines the active workspace directory and storage layout:

- **Workspace Identification**:
  - Pinned workspace via `--workspace <path-or-name>` or `-w <name>`.
  - Protocol-based remote workspace (e.g. `http://` or `ssh://`).
  - Default workspace: `~/.config/nuts/default-workspace` (or OS equivalent).
- **Isolation Modes**:
  - `SYSTEM`: Global shared workspace (`/etc/opt/nuts` on Linux/Unix, `C:\Program Files\nuts` on Windows).
  - `USER`: Per-user isolated workspace (`~/.nuts` or standard XDG directories).
  - `SANDBOX`: Completely isolated temporary directory (`nuts-sandbox-...`), using `STANDALONE` storage and deleted upon JVM termination.
  - `MEMORY`: In-memory temporary workspace.
- **Store Location Mapping**:
  Computes physical paths for each functional store based on the active strategy (`EXPLODED` or `STANDALONE`):
  - `BIN`: Executable binaries and scripts.
  - `CONF`: Workspace configuration (`nuts-workspace.json`, API/runtime descriptors).
  - `LIB`: Cached JARs, POMs, and library binaries.
  - `VAR`: Application databases and runtime state.
  - `LOG`: Log and trace files.
  - `TEMP`: Temporary scratch folders.
  - `CACHE`: Ephemeral index caches.
  - `RUN`: Process IDs, named pipes, and socket files.

### Step 3: Repository Discovery

The bootstrapper resolves the list of repositories used to download `nuts-runtime` and dependencies:
1. **Previous Workspace Configuration**: If the workspace was previously initialized, loads repository definitions from `nuts-workspace.json`.
2. **Default Repositories**: If creating a fresh workspace, uses built-in defaults:
   - `maven`: Local Maven cache (`~/.m2/repository`).
   - `central`: Maven Central (`https://repo.maven.apache.org/maven2`).
   - `nuts-preview`: Nuts Preview Repository (enabled by default or via `--preview-repo`).
3. **Repository Descriptors**: Reads local `.nuts-repository` metadata files when referencing directory-based repositories.

### Step 4: API Version Resolution

The bootstrapper identifies the target `nuts-api` version:
- **Explicit Version**: Specified via `--api-version <version>`.
- **`LATEST` or `RELEASE`**: Queries configured repositories for the highest available version in `maven-metadata.xml`.
- **Embedded Default**: Falls back to the bootstrapper's own built-in `NUTS_BOOT_VERSION`.
- **Inherited Session**: When executing embedded within an existing nuts process, strictly enforces `NUTS_BOOT_VERSION` to prevent classloader collisions.

### Step 5: Runtime Version & Dependency Resolution

Once the API version is determined (e.g., `1.0.0`):
1. **Version Prefix Matching**: The bootstrapper looks for `net.thevpc.nuts:nuts-runtime` where the runtime version starts with `<apiVersion>.` (e.g., `1.0.0.0`, `1.0.0.1`).
2. **Local-First Resolution**:
   - Checks the workspace's local `LIB/id/` cache.
   - If not found or if cache is expired (`--expire`), queries remote repositories for the latest matching version.
   - If offline and remote queries fail, inspects the local fallback cache map (`getFallbackCache`).
3. **Dependency Descriptor**: Loads the runtime descriptor (`.nuts` JSON descriptor or `pom.xml`) containing the runtime's transitive dependencies.

### Step 6: ClassLoader & ClassWorld Construction

1. **Artifact Verification & Download**:
   Missing JARs are downloaded into the workspace `LIB` store (`<lib-store>/id/<group-path>/<artifact>/<version>/<artifact>-<version>.jar`).
2. **`NBootClassLoader` Creation**:
   Constructs an isolated classloader tree (`NBootClassLoader`) containing:
   - `nuts-runtime.jar`
   - Runtime dependencies (e.g., internal parsers, terminal formatters, SSH/crypto helpers).
   - Parent ClassLoader: Context ClassLoader / System ClassLoader.

### Step 7: Factory SPI Discovery & Handoff

1. **ServiceLoader Discovery**:
   Executes `ServiceLoader.load(NBootWorkspaceFactory.class, workspaceClassLoader)` to find workspace factory implementations inside the loaded `nuts-runtime.jar`.
2. **Priority Ordering**:
   Sorts discovered factories using `NBootWorkspaceFactoryComparator` (which evaluates supported API versions, runtime architectures, and priority scores).
3. **Workspace Instantiation**:
   Calls `factory.createWorkspace(bootOptions)` to instantiate `NWorkspaceBase`.

### Step 8: Execution

With the runtime loaded:
- If `--version` or `--help` was requested, outputs formatted information and exits.
- If `--nuts-exec-mode=complete` was requested, generates auto-completion suggestions.
- Otherwise, executes `workspace.runBootCommand()`, handing full control over to `nuts-runtime`.

---

## Multi-Process Execution & Delegation

When the requested execution requirements cannot be satisfied in the current JVM process, `NBootWorkspaceImpl` automatically delegates to a child JVM process via `runNewProcess()`:

```text
Current JVM (e.g. Java 17, nuts-boot 1.0.0)
     │
     ├─ Requires Java 8? ────────► Spawns `java-8 -jar nuts.jar ...`
     ├─ Custom JVM options? ─────► Spawns `java [custom-opts] -jar nuts.jar ...`
     └─ Target API mismatch? ────► Spawns `java -jar <resolved-api-nuts.jar> ...`
```

### Requirement Triggers
- **Java Executable**: Different JVM executable requested via `--java-command <path>`.
- **JVM Options**: Specific memory/GC/system options requested via `--java-options <options>`.
- **API Version**: Target API version differs from `NUTS_BOOT_VERSION`.

---

## Recovery and Maintenance Flags

| Flag | Description | Action Taken by `NBootWorkspaceImpl` |
|---|---|---|
| `--expire` | Forces expiration of cached metadata | Bypasses local timestamp cache checks and re-validates repository metadata. |
| `--recover` | Recovers a corrupted workspace | Clears `CACHE`, `TEMP`, and deletes cached `nuts-runtime.jar` binaries from `LIB`, forcing a clean re-download. |
| `--reset` | Reinitializes workspace | Deletes workspace configuration and cache while preserving user data and previous repository configurations. |
| `--reset-hard` | System-wide reset | Deletes all workspace store locations and restores the system to a clean state. |
| `-N` / `--dry` | Dry run | Simulates bootstrap actions without modifying the filesystem. |

---

## Error Diagnostics & Troubleshooting

When bootstrap fails (e.g. network failure or missing runtime), `NBootWorkspaceImpl` logs a comprehensive diagnostics block containing:
- Exact `nuts-boot` version and API version.
- Resolved workspace locations and storage layout.
- Active repositories and connection timeout settings.
- JVM version, executable path, classpath, and OS properties.
- Full exception stack traces with component context.
