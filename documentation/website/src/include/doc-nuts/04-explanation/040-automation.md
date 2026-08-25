---
id: automation
title: Automation
sidebar_label: Automation & DevOps
---

**nuts** has been designed and implemented with automation, DevOps, and scripting in mind. It empowers users and platform engineers to automate application management, deployment workflows, and toolchains with a platform-independent, secure, and extensible architecture.

The **nuts** Application Framework provides seamless support for process automation through structured output, non-interactive execution modes, workspace isolation, and dependency-driven classloading.

You can invoke standard system commands and retrieve output directly in structured formats such as JSON, XML, YAML, or TSON. This enables powerful data manipulation using pipes, just as in POSIX shell environments — but enriched with structured data semantics.

Beyond command automation, **nuts** dynamically resolves and loads dependencies at runtime. For example, installing a specific Tomcat version compatible with your current JRE is as simple as one automated call — no manual download or configuration needed.

## Install Automation

Installation commands in **nuts** are inherently scriptable and perfectly suited for CI/CD pipelines. You can install, update, or remove packages using declarative commands with predictable outputs.

Example:

```bash
nuts --bot --yes install tomcat --sudo
```

This command will automatically:
- Resolve the appropriate version of Tomcat for your current Java environment.
- Download and install the binary securely from configured repositories.
- Provision the appropriate JDK to run the tomcat version
- Elevate privileges if required (`--sudo`) to register system-level services.

Using the `--bot` (non-interactive mode) and `--yes` (auto-confirm) flags guarantees that the process will never hang waiting for a user prompt, making it safe for headless automation.

## Structured Output

Unlike traditional CLI tools that emit raw string data requiring fragile `grep` and `awk` parsing, every command in **nuts** can emit results in highly structured formats:

- `--json`
- `--yaml`
- `--xml`
- `--tson` (Typed Superset of JSON)
- `--plain` (default)

Example:

```bash
nuts --bot --json - ls . | jq '.[] | select(.size > 1024)'
```

This capability enables robust, failure-resistant pipelines by piping exact data models directly into JSON processors like `jq`, YAML processors like `yq`, or custom deployment scripts.

## Environment Sensitive

**nuts** intelligently detects and adapts to the surrounding hardware and OS environment, including:

- **Architecture**: x86_64, ARM, aarch64, Itanium.
- **Operating System**: Linux, Windows, macOS, BSD.
- **Shell**: Bash, Zsh, CMD, PowerShell.
- **Runtime Platforms**: Java versions (8 through 24).
- **Desktop Environments**: GNOME, KDE, Windows Shell.

This environmental awareness allows you to write a single portable automation script that behaves correctly whether it runs on an M-series Mac developer laptop, a Windows VM, or an ARM-based Linux container.

## Multi Platform

Because **nuts** runs on the JVM, it executes consistently across virtually any platform:

- Linux, macOS, Windows, BSD, etc.
- CLI-compatible on both GUI and headless server systems.

Cross-platform consistency ensures that your automation logic avoids complex branching in your deployment scripts.

## Workspace Isolation

Workspaces provide strictly isolated environments containing their own repositories, installed tools, and configurations.

This isolation is a superpower for automation. It ensures that different applications, teams, or pipeline stages (e.g., build vs integration testing) can coexist on the same CI agent without classpath contamination or version collisions.

```bash
nuts -w ci-pipeline-123 install my-build-tool
```

## Security Mechanisms

**nuts** includes several mechanisms to ensure secure automation:

- **Permission model** for restricting access to repositories, settings, and installations.
- **Execution contexts** that support privilege separation (e.g., user vs system installs).
- **Sandboxing** of downloaded tools and runtime dependencies.
- **Authentication and Authorization** for protected resources (private registries, remote configs).

These features are especially valuable in enterprise, CI/CD, or public/shared environments.

## Design Patterns for Automation

To maximize reliability in automated environments, adopt these standard **nuts** patterns:

### 1. Ephemeral Workspaces
In CI/CD environments, always generate a unique, temporary workspace for the job to avoid cache poisoning:
```bash
export NUTS_WORKSPACE="temp-$GITHUB_RUN_ID"
nuts -w $NUTS_WORKSPACE run my-test-suite
```

### 2. Version Pinning for Reproducibility
Avoid floating versions in scripts. Always specify exact coordinates to ensure builds are deterministic over time:
```bash
nuts --bot install org.company:server-app#2.4.1
```

### 3. Idempotent Executions
Use the `--dry` flag in pre-flight checks to safely evaluate what an installation command *will* do before it mutates the system state, allowing scripts to verify dependencies dynamically.

### Pipeline Examples

**GitHub Actions:**
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install toolchain via Nuts
        run: |
          nuts -w temp-${{ github.run_id }} --bot install my.group:toolchain#1.2.0
          nuts -w temp-${{ github.run_id }} --bot run toolchain build
```

**Bash Script Deployment:**
```bash
#!/bin/bash
set -e

WORKSPACE="prod-env"
APP="com.company:web-service#3.1.2"

echo "Deploying $APP to workspace $WORKSPACE..."
nuts -w "$WORKSPACE" --bot --yes install "$APP"
nuts -w "$WORKSPACE" --bot run "$APP" start
```
