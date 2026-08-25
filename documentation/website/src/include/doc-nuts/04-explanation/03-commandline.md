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
These options define configuration when a workspace is newly initialized. If the workspace already exists, these options are ignored. They are not inherited by sub-processes.
| Option | Example | Description |
|---|---|---|
| `--archetype` | `--archetype=server` | Sets the initial workspace template |
| `--store-strategy` | `--store-strategy=standalone` | Defines the file system layout |
| `--install-companions`| `--install-companions` | Installs tools like nmvn and nsh |

### Runtime Options
These control the execution environment of the current command but are not passed down to child processes.
| Option | Example | Description |
|---|---|---|
| `--reset` | `nuts --reset run app` | Clears cache before running |
| `--recover` | `nuts --recover run app` | Disables network, relies on cache |
| `--dry` | `nuts --dry install app` | Simulates the command without side effects |
| `--version` | `nuts --version` | Prints the runtime version |

### Exported Options
These options affect both the current process and any sub-processes spawned by **nuts** (such as running `nsh` or an application).
| Option | Example | Description |
|---|---|---|
| `--workspace` | `-w my-workspace` | Targets a specific workspace |
| `--bot` | `--bot` | Enables non-interactive automation mode |
| `--color` | `--color=always` | Forces colored output down the process tree |
| `--global` | `--global` | Runs in global system context |

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
