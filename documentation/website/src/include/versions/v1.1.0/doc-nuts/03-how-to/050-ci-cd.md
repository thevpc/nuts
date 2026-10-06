---
title: CI/CD Integration
---

## Overview

Integrating **nuts** into Continuous Integration and Continuous Deployment (CI/CD) pipelines allows you to reliably manage build tools, run test suites, and deploy applications across various environments. Because **nuts** ensures deterministic dependency resolution and workspace isolation, it is an ideal fit for automated workflows.

## Key Flags for CI/CD

When running **nuts** in non-interactive pipeline environments, certain flags are essential to ensure commands complete successfully without waiting for user input:

* `--bot`: Enables bot mode. This disables all interactive prompts, progress bars, and ANSI color codes, ensuring logs are clean and machine-readable.
* `--yes` or `-y`: Automatically confirms all prompts with a affirmative response. Essential for unattended installations and updates.
* `--json`: Outputs command results in structured JSON format, making it easy to parse data using tools like `jq` in your pipeline scripts.

## CI/CD Platform Examples

### GitHub Actions

Here is a complete GitHub Actions workflow that sets up a Java environment, initializes a **nuts** workspace, caches it for performance, and executes a tool:

```yaml
name: Nuts CI Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache Nuts Workspace
        uses: actions/cache@v4
        with:
          path: ~/.nuts
          key: ${{ runner.os }}-nuts-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-nuts-
            
      - name: Install and Run Tool
        run: |
          curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash
          nuts --bot --yes install my.group:my-tool
          nuts --bot --yes my-tool --test
```

### GitLab CI

For GitLab CI, you can leverage a base image and caching definitions in your `.gitlab-ci.yml`:

```yaml
image: eclipse-temurin:21-jdk

variables:
  NUTS_WORKSPACE: "$CI_PROJECT_DIR/.nuts-workspace"

cache:
  paths:
    - .nuts-workspace/

before_script:
  - curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash
  - export PATH="$PATH:$HOME/.local/share/nuts/apps/bin"

test_job:
  stage: test
  script:
    - nuts --bot -y -w $NUTS_WORKSPACE install my.group:my-tool
    - nuts --bot -y -w $NUTS_WORKSPACE exec my.group:my-tool --validate
```

### Jenkins

In a `Jenkinsfile`, you can run **nuts** inside a shell step, ensuring you pass the necessary non-interactive flags:

```groovy
pipeline {
    agent { docker { image 'eclipse-temurin:21-jdk' } }
    
    stages {
        stage('Initialize Nuts') {
            steps {
                sh 'curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash'
            }
        }
        stage('Execute Tool') {
            steps {
                sh 'nuts --bot -y install my.group:my-tool'
                sh 'nuts --bot -y my-tool --report=target/report.xml'
            }
        }
    }
}
```

## Caching Strategies

To speed up CI/CD execution times, you should heavily cache the **nuts** workspace directory. By default, this is located at `~/.nuts` (or the equivalent XDG data directories on Linux). Caching this directory prevents your pipeline from re-downloading Java artifacts and bootstrap engines on every commit.

## Ephemeral Workspaces

If you require strict isolation between jobs running on the same persistent runner, you can instruct **nuts** to use a temporary, isolated workspace using the `-w` (workspace) flag:

```bash
nuts -w /tmp/workspace-$BUILD_ID --bot -y my-tool
```

This guarantees that the execution starts from a clean slate, unaffected by previous pipeline runs, while still benefiting from any globally configured repositories.

## Structured Output for Parsing

When writing bash scripts for your pipelines, parsing raw text output is fragile. Instead, use the `--json` flag to retrieve structured data and process it with `jq`:

```bash
# Check if an artifact is installed
IS_INSTALLED=$(nuts search --installed my.group:my-tool --json | jq 'length > 0')

if [ "$IS_INSTALLED" = "true" ]; then
    echo "Tool is ready!"
fi
```

## Artifact Verification

CI/CD pipelines are excellent places to enforce security and integrity checks. You can use **nuts** to verify deployed artifacts or check for configuration drift before executing critical deployment scripts:

```bash
# Display detailed information about the resolved artifact before execution
nuts info my.group:my-tool --json
```
