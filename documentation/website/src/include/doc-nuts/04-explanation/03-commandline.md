---
id: commandline
title: Command Line Arguments
sidebar_label: Command Line Arguments
---

**nuts** employs a robust and highly structured command-line argument parser. This format is standardized across the **nuts** Application Framework (NAF), meaning all NAF-built applications share the same predictable argument syntax.

In **nuts**, arguments are broadly categorized as either Options (prefixed with hyphens) or Non-options (arguments like application names or paths).

## Quick Reference

| Prefix      | Type          | Example                | Description                    |
|-------------|---------------|------------------------|--------------------------------|
| `-`         | Short Option  | `-w`, `-y`             | Single character flags         |
| `--`        | Long Option   | `--workspace`, `--bot` | Verbose flags for scripts      |
| `---`       | Custom Option | `---perf`              | Internal or experimental flags |
| `-X` / `-D` | Executor      | `-Xmx2G`, `-Dfoo=bar`  | Passed directly to the JVM     |
| `--//`      | Comment       | `--// ignore this`     | Ignored by the parser          |

## Short vs Long Options

Options can be long (starting with a double hyphen `--`) or short (starting with a single hyphen `-`). 
Many arguments support both forms for convenience. For instance, `-w` and `--workspace` are equivalent ways to specify the workspace location.

## Valued / Non-valued Options

Options can accept values (strings, integers, etc.). The value can be supplied immediately after the option separated by a space, or joined using an `=` sign.

All of the following are equivalent:

```bash
nuts -w=/opt/workspace
nuts -w /opt/workspace
nuts --workspace /opt/workspace
nuts --workspace=/opt/workspace
```

## Boolean Options

Boolean options are special. If provided without a value, they are implicitly evaluated as `true`. Therefore, `--bot` and `--bot=true` are functionally identical.

However, notice that `--bot true` is **not** equivalent. Because the option expects a boolean binding, passing `true` as a separated token will result in the parser treating `true` as a separate non-option argument.

To explicitly pass `false` to a boolean option, you can suffix it with `=false` or use a logical NOT prefix (`!` or `~`). The `~` symbol is provided as a safe alternative to `!` because some shells (like Bash) intercept `!` for history expansion.

The following are equivalent ways to disable an option:
```bash
nuts --bot=false
nuts --!bot
nuts --~bot
```

## Combo Simple Options

Short options can be clustered together in a single block. For example, `-ls` is parsed identically to `-l -s`. 

*Note:* For standard portability, `-version` is treated as a single special short option, not a combination of `-v -e -r...`

## Ignoring Options / Comments

Sometimes you need to temporarily disable an option in a script or pass explanatory text. Any option starting with `-//` or `--//` is treated as a comment and is completely ignored by the parser.

## Nuts Option Types

Options processed by the core **nuts** engine fall into specific behavioral categories.

### Create Options
These options define configuration when a workspace is newly initialized. If the workspace already exists, these options are ignored.
| Option | Example | Description |
|---|---|---|
| `--archetype` | `--archetype=server` | Sets the initial workspace template (`default`, `minimal`, `server`) |
| `--store-strategy` | `--store-strategy=standalone` | Defines the file system layout (`exploded`, `standalone`) |
| `--install-companions`| `--install-companions` | Installs tools like `nmvn` and `nsh` |
| `--java-home` | `--java-home=/path/to/jdk` | Specifies the Java installation path to run the workspace |
| `--solver` | `--solver=maven` | Configures dependency solver (`maven`, `gradle`, `descriptor`, `maven-first`, `gradle-first`) |

### Isolation Levels
Isolation options define the workspace boundary and disk persistence behavior.
| Option | Example | Description |
|---|---|---|
| `--sandbox` | `--sandbox` | Runs in a temporary location with a fresh instance each time. No disk persistence on exit. |
| `--in-memory` | `--in-memory` | Runs the workspace entirely in memory without creating any disk configuration. Only temporary downloads folder used. |
| `--confined` | `--confined` | Runs in a specific location as regular user without modifying global shortcuts, `.bashrc`, or user environment. |
| `--isolation-level` | `--isolation-level=user` | Explicitly sets workspace isolation level (`system`, `user`, `confined`, `sandbox`, `memory`) |

### Open Modes
Open mode flags control workspace creation vs opening behavior.
| Option | Example | Description |
|---|---|---|
| `--open-or-create` | `--open-or-create` | Default mode: opens existing workspace or creates a new one if missing |
| `--open` | `--open` | Opens existing workspace; throws an error if workspace does not exist |
| `--create` | `--create` | Creates a new workspace; throws an error if workspace already exists |
| `--open-or-null` | `--open-or-null` | Opens existing workspace; exits quietly without error if workspace does not exist |

### Runtime & Execution Options
These control the execution environment of the current command.
| Option | Example | Description |
|---|---|---|
| `--reset` / `-Z` | `nuts -Z install app` | Clears cache and re-bootstraps workspace before running |
| `--reset-hard` | `nuts --reset-hard` | Wipes all workspace directories and configuration files |
| `--reset-options` | `nuts --reset-options` | Resets all previously supplied option flags to default values |
| `--recover` / `-z` | `nuts -z install app` | Recovers corrupted workspace by ignoring cache and re-resolving |
| `--read-only` / `-R` | `nuts -R exec app` | Runs workspace in read-only mode; changes are not saved to disk |
| `--offline` / `-F` | `nuts -F search app` | Disables remote repository access during execution |
| `--dry` / `-D` | `nuts -D install app` | Simulates the command without performing side effects |
| `--stacktrace` / `-d`| `nuts -d exec app` | Prints full Java stacktrace on errors |
| `--debug` | `nuts --debug exec app` | Enables JDWP debugging for current process and spawned child processes |

### Exported Options
These options affect both the current process and any sub-processes spawned by **nuts** (such as running `nsh` or an application).
| Option | Example | Description |
|---|---|---|
| `--workspace` / `-w` | `-w my-workspace` | Targets a specific workspace location or name |
| `--bot` / `-B` | `--bot` | Enables non-interactive automation mode with structured output |
| `--color` / `-c` | `--color=always` | Forces colored output down the process tree |
| `--global` / `-g` | `--global` | Runs in global system-wide context (`/opt/nuts`) |

### Executor Options
These are intercepted and passed directly to the underlying package executor (usually the Java Virtual Machine).
| Option | Example | Description |
|---|---|---|
| `-X...` | `-Xmx1G` | Sets JVM memory limits |
| `-D...` | `-Djava.security.policy=...`| Sets JVM system properties |

### Custom / Internal Options
Special options used for internal profiling, debugging, or experimental features. They begin with a triple hyphen (`---`). The parser will never throw an error for these; if they are unrecognized, they are safely ignored.
| Option | Example | Description |
|---|---|---|
| `---perf` | `nuts ---perf` | Outputs execution performance metrics |
| `---show-command` | `nuts ---show-command run app`| Prints the exact JVM command used to launch the app |
| `---init-java` | `nuts ---init-java` | Initializes Java environment bindings |
| `---monitor.enabled`| `nuts ---monitor.enabled` | Enables internal telemetry |

### Application Options
Standard options processed by the application built on the framework.
| Option | Example | Description |
|---|---|---|
| `--help` | `nuts --help` | Displays command usage and reference |
| `--version` | `nuts --version` | Outputs application version information |

All **nuts** options are thoroughly described in the built-in command help. Just type:
```bash
nuts --help
```
