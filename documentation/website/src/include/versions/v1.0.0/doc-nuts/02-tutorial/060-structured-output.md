---
title: Structured Output
---

:::tip What You'll Learn
In this section, you will learn how to extract machine-readable data from **nuts**. We will cover:
* How to format command output using standard data structures (JSON, YAML, XML, etc.).
* Using Bot Mode for automation and CI/CD environments.
* Piping formatted output to command-line processing tools like `jq` and `yq`.
:::

## Overview

A core philosophy of **nuts** is that it should be just as easy for a script to use as it is for a human. To support this, **every built-in nuts command** can emit its output in multiple structured formats. 

Instead of writing complex `grep` and `awk` commands to parse plain text, you can ask **nuts** to output JSON or YAML directly, making integration with other tools and scripts completely frictionless.

## Available Formats

You can change the output format of any command by passing the format flag *before* the command name. The supported formats are:

* `--plain` (Default): Human-readable text format.
* `--json`: Standard JSON array/object format.
* `--yaml`: Standard YAML format.
* `--xml`: Standard XML format.
* `--table`: Tabular text format (great for terminal readability).
* `--tree`: Hierarchical tree format (useful for dependency graphs).
* `--props`: Java properties format (`key=value`).
* `--tson`: Typed String Object Notation (a custom compact format).

## Examples in Action

Let's look at how the same command behaves when we change the output format. We will use `nuts search --installed`, which lists the artifacts currently installed in the workspace.

### JSON Output
```bash
nuts --json search --installed netbeans-launcher
```

**Output:**
```json
[
  {
    "id": "net.thevpc.app:netbeans-launcher#1.2.2",
    "name": "netbeans-launcher",
    "version": "1.2.2",
    "status": "installed"
  }
]
```

### YAML Output
```bash
nuts --yaml search --installed netbeans-launcher
```

**Output:**
```yaml
- id: "net.thevpc.app:netbeans-launcher#1.2.2"
  name: "netbeans-launcher"
  version: "1.2.2"
  status: "installed"
```

### Table Output
```bash
nuts --table search --installed netbeans-launcher
```

**Output:**
```text
ID                                      Version  Status
--                                      -------  ------
net.thevpc.app:netbeans-launcher        1.2.2    installed
```

## Bot Mode for CI/CD

When you are writing scripts for a Continuous Integration/Continuous Deployment (CI/CD) pipeline, you want to ensure that the CLI tool never hangs waiting for user input and doesn't pollute the logs with ANSI color codes.

**nuts** provides a dedicated `--bot` mode for this exact scenario. When you include the `--bot` flag:

- 2. All interactive prompts are disabled (it assumes "yes" or default answers).
- 2. Terminal color formatting is stripped out.
- 3. Progress bars are disabled.

```bash
nuts --bot --json install my-automated-tool
```
*(Alternatively, you can also use `--yes` or `-y` if you just want to auto-confirm prompts without stripping colors).*

## Piping to Processing Tools

Because **nuts** generates standard structured data, you can seamlessly pipe its output into popular command-line JSON/YAML processors.

### Using `jq` for JSON
If you want to extract just the artifact IDs from your installed applications, you can pipe the JSON output directly to `jq`:

```bash
nuts --json search --installed | jq -r '.[].id'
```

### Using `yq` for YAML
Similarly, if you are querying workspace configurations and prefer YAML, you can use `yq`:

```bash
nuts --yaml settings list repos | yq '.[].url'
```

### Using `xmlstarlet` for XML
For legacy systems or enterprise environments that rely heavily on XML:

```bash
nuts --xml search --installed | xmlstarlet sel -t -v "//id"
```

## Practical Use Cases

Structured output enables powerful automation workflows:

* **Monitoring Scripts**: A cron job can run `nuts --json update --check` and send the resulting JSON to a monitoring dashboard or Slack bot to alert the team about available updates.
* **CI/CD Pipelines**: A GitHub Action can dynamically resolve the latest version of a tool using `nuts --json search my-tool` and parse the output to configure the rest of the build matrix.
* **Workspace Auditing**: Security teams can run `nuts --json search --installed` to generate a machine-readable Software Bill of Materials (SBOM) of everything currently active in the workspace.
