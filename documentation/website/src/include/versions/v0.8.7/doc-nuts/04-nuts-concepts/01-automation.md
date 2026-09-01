---
id: automation
title: Automation
sidebar_label: Automation & DevOps
---


`nuts` has been designed and implemented with automation, DevOps, and scripting in mind. It empowers users and developers to automate application management, DevOps workflows, and toolchains with a platform-independent, secure, and extensible architecture.

The `nuts` Application Framework provides seamless support for process automation with structured input/output, workspace isolation, multi-environment adaptability, and dependency-driven runtime classloading.

You can invoke standard system commands (like ls) and retrieve output directly in structured formats such as JSON, XML, YAML, or TSON. This enables powerful data manipulation using pipes, just as in POSIX shell environments — but enriched with structured data semantics.

Beyond command automation, `nuts` dynamically resolves and loads dependencies at runtime. For example, installing a specific Tomcat version compatible with your current JRE is as simple as one automated call — no manual download or configuration needed.

Automation is further reinforced through workspaces, sandboxing, security controls, and environment detection to ensure your automation logic remains portable, secure, and adaptable across diverse platforms and environments.


## Install Automation
Installation commands in `nuts` are inherently scriptable and suitable for automation pipelines. You can install, update, or remove packages using declarative commands with predictable outputs.

Example:

```bash
nuts --bot --yes install tomcat --sudo
```

This command will automatically:

- Resolve the appropriate version of Tomcat for your current Java environment
- Download and install it (using local or remote repositories)
- Make it available to the current workspace or system scope

Using --yes and --bot flags ensures full non-interactive automation.


## Structured Output
Every command in `nuts` can emit results in multiple structured formats:

- `--json`
- `--yaml`
- `--xml`
- `--tson` (Typed Superset of JSON)
- `--plain` (default)

Example:

```bash
nuts --bot --json - ls . | jq '.[] | select(.size > 1024)'
```

This enables powerful pipelines with JSON processors like `jq`, YAML processors like `yq`, or other tools compatible with structured formats.


## Environment Sensitive
`nuts` detects and adapts to the surrounding environment, including:

- Architecture (x86_64, ARM, Itanium, etc.)
- Operating system (Linux, Windows, macOS)
- Shell (bash, zsh, cmd, PowerShell)
- Runtime platforms (Java, .NET, etc.)
- Desktop environments (GNOME, KDE, Windows Shell, etc.)

This allows you to write portable scripts that behave consistently across platforms, with minimal branching.



## Multi Platform
You can run `nuts` on virtually any platform with a Java runtime:

- Linux, macOS, Windows, BSD, etc.
- Java 8+, fully tested up to Java 24
- CLI-compatible on both GUI and headless systems

Cross-platform consistency ensures your automation logic works the same on a developer laptop, a CI/CD pipeline, or a containerized deployment.


## Workspace Isolation
Workspaces in `nuts` provide isolated environments with their own:
- Repositories
- Installed tools
- Configuration settings

This ensures that different applications, teams, or environments (e.g., staging vs production) can coexist on the same system without interference. It also facilitates reproducibility and clean state resets.

```bash
nuts -w my-workspace install my-tool
```


## Security Mechanisms
`nuts` includes several mechanisms to ensure secure automation:

- Permission model for restricting access to repositories, settings, and installations
- Execution contexts that support privilege separation (e.g., user vs system installs)
- Sandboxing of downloaded tools and runtime dependencies
- Authentication and Authorization for protected resources (private registries, remote configs)

These features are especially valuable in enterprise, CI/CD, or public/shared environments.
