---
title: Create Bundles
---

## Create an Air-Gapped Bundle

A **bundle** is a self-contained, highly portable package of a **nuts** workspace that includes an application and all its dependencies. Bundles are designed to run on machines without internet access or an existing **nuts** installation, solving the "fat-JAR" problem while keeping the target workspace isolated.

### Creating a Bundle

To create a bundle, use the `nuts bundle` command with one or more application ids:

```bash
nuts bundle myapp#1.2.3
```

By default this produces a single executable jar named after the resolved application name and version (e.g. `myapp-1.2.3-bundle.jar`) in the current directory. You can set an explicit destination with `--target`:

```bash
nuts bundle myapp#1.2.3 --target myapp-bundle.jar
```

### What's Inside a Bundle?

A jar/zip bundle packages everything required to execute the application:
* A minimal embedded **nuts** bootstrap runner (`NutsBundleRunner`)
* The application artifact(s) and all resolved dependencies, laid out under `META-INF/bundle`
* Per-OS launcher scripts (`.sh` for Linux/macOS/Unix, `.bat` for Windows)
* Bundle metadata (`nuts-bundle-info.config`, `nuts-bundle-files.config`) describing what to install and where

### Running a Bundle on the Target Machine

Deploying a bundle requires nothing more than a standard Java installation on the target machine — no **nuts** install, no repository configuration, no internet connection.

For a jar bundle, run it like any standard executable jar:

```bash
java -jar myapp-bundle.jar
```

For a `--dir`/`--exploded` bundle, run the generated launcher script for your platform instead (installed at the target root, named after the application, e.g. `myapp` / `myapp.bat`).

On first run, the bundle recreates an isolated **nuts** workspace under `.nuts-bundle/` next to the launcher (`.nuts-bundle/lib` as the repository, `.nuts-bundle/ws` as the workspace), installs the embedded artifacts from the bundle, and launches the application — all without touching your normal **nuts** installation or reaching out to the network.

### Bundle Formats

Choose the output layout with one of:

* `--jar` / `--as-jar` *(default)* — a single executable jar
* `--zip` / `--as-zip` — a zip archive with the same contents
* `--exploded` / `--as-exploded` — an unpacked folder that still carries the bundle metadata files, so it can be cleaned (`--clean`) and rebuilt in place
* `--dir` / `--as-dir` — a plain unpacked folder with no bundle metadata files

```bash
nuts bundle myapp#1.2.3 --exploded --target ./myapp-bundle --clean
```

### Multiple Applications and Extra Libraries

You can bundle several executable applications together — each gets its own launcher script:

```bash
nuts bundle app-one#1.0 app-two#2.0
```

Use `--lib` to pull in an additional id purely as a dependency, without generating a launcher for it:

```bash
nuts bundle myapp#1.2.3 --lib my-group:extra-plugin#2.0
```

### Naming and Metadata

The default bundle name is derived from the resolved application name and version, but can be overridden:

* `--app-name` / `--name` — application name used in the default file/folder name
* `--app-version` — application version used in the default file/folder name
* `--app-title` / `--title` — title stored in the bundle info
* `--app-desc` / `--desc` — description stored in the bundle info

### Runtime Behavior Flags

A few options control how the *embedded* workspace behaves when the bundle is later executed, not how it's built:

* `--embedded` — run the embedded workspace in embedded mode
* `-y`, `--yes` — auto-confirm prompts on execution
* `-z`, `--reset` — reset the embedded workspace on execution
* `-l`, `--verbose` — run the embedded workspace verbosely on execution

### Use Cases

* **Air-gapped servers** — deploy to environments with no network access
* **Security-restricted environments** — avoid dependency resolution blocked by enterprise firewalls
* **Portable distribution** — ship a single file without requiring recipients to understand package management
* **Offline demos** — run demonstrations without relying on venue connectivity