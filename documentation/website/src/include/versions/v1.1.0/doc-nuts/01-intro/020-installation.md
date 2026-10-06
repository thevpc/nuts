---
id: installation
title: Installation
---

## Interactive Download Center

For interactive platform selection, custom release channels, standalone executables, GUI installers, bundled JRE packages, and Docker commands:

:::tip Interactive Download Wizard

Visit the official **[Nuts Download & Release Hub](/download.html)** to choose your version, platform, and preferred installation package.

:::

---

## Quick Terminal Install

If you prefer installing directly from your command line:

```bash
# Linux / macOS (Stable Release)
curl -s https://thevpc.net/nuts/install-stable.sh | bash

# Linux / macOS (Latest Release)
curl -s https://thevpc.net/nuts/install-latest.sh | bash

# Windows (Command Prompt / PowerShell)
# Download nuts-app-latest.jar and run:
java -jar nuts-app-latest.jar -Zy
```

:::important

After installation, **restart your terminal session** so that environment paths and shell integration take effect.

:::

---

## System Requirements

**nuts** is lightweight and designed to run on any major operating system:

* **Java**: Requires Java Runtime Environment (JRE) or Java Development Kit (JDK) version **8** or higher (tested up to Java **25**). For Java 1.8, update 150+ is required.
* **Disk Space**: ~15MB for the minimal **nuts** installation. Additional space is used for your local package cache (typically 100MB to 500MB depending on usage).
* **Memory**: Operates comfortably in ~300MB RAM when JVM heap is capped (e.g. `NUTS_JAVA_OPTIONS=-Xmx32m`). By default, HotSpot dynamically sizes memory according to total system RAM.
* **Operating System**: Linux (all distributions), macOS (Intel & Apple Silicon), Windows 7 and later.

Verify your local Java version before installing:

```bash
java -version
```

---

## Verifying Installation

To verify that **nuts** is correctly installed and accessible in your `PATH`, open a new terminal and run:

```bash
nuts --version
```

Output will display the installed API and runtime implementation versions:

```text
{{apiVersion}}/{{runtimeVersion}}
```

## Next Steps

Now that **nuts** is installed, proceed to the [First App](030-first-app.md) guide to install and launch your first package.
