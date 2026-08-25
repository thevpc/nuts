---
id: installation
title: Installation
---

## LTS vs Latest

| Channel | Source | Cadence | Use when... |
|---------|--------|---------|-------------|
| **LTS** | Maven Central | Semi-annual | Production, CI/CD, reproducible builds |
| **Latest** | maven.thevpc.net | Semi-monthly | Evaluating features, development, bug fixes |

## With or without JRE

| Installer               | Size   | Use when...                              |
|-------------------------|--------|------------------------------------------|
| Standard (~5MB)         | Small  | You already have Java 8+ installed       |
| +JRE (~40MB)            | Medium | You need a bundled runtime               |
| Native (.msi/.dmg/.rpm) | Varies | Enterprise deployment, no `curl \| bash` |

## Quick Install

For most users, this is the recommended installation method. If you want the most rock-solid, production-ready release, use the LTS (Stable) channel. For the newest features and recent bug fixes, use the Preview (Latest) channel.

```bash
# Linux/macOS (LTS / Stable)
curl -s https://thevpc.net/nuts/install-stable.sh | bash

# Linux/macOS (Preview / Latest)
curl -s https://thevpc.net/nuts/install-latest.sh | bash

# Windows
# Download the installer from the interactive page below
```

## System Requirements

**nuts** is lightweight and designed to run practically anywhere:

- **Java**: Requires a valid Java Runtime Environment (JRE) or Java Development Kit (JDK) version **8** or above (tested against Java **25**). Note that Java 1.8 requires update 150+. 
- **Memory**: Minimal footprint; no strict RAM requirements.
- **Disk Space**: ~5MB for the **nuts** binary itself. Additional space is used for your local workspace cache (expect ~500MB depending on usage).
- **Operating System**: Runs on any OS with Java support, including all recent versions of Linux, macOS, and Windows.

To verify your Java installation, run:

```bash
java -version
```

You should see output similar to this (ensure the version is 1.8 or higher):

```bash
$ java -version
openjdk version "24.0.1" 2025-04-15
OpenJDK Runtime Environment (build 24.0.1+9-suse-1.1-x8664)
OpenJDK 64-Bit Server VM (build 24.0.1+9-suse-1.1-x8664, mixed mode, sharing)
```

## Installation

<Tabs
defaultValue="linux"
values={[
{ label: 'Linux', value: 'linux', },
{ label: 'MacOS', value: 'macos', },
{ label: 'Windows', value: 'windows', },
{ label: '*NIX wget', value: 'wget', },
{ label: '*NIX curl', value: 'curl', }
]}
>
<TabItem value="windows">

Download [nuts-app-{{apiVersion}}.jar]({{latestJarLocation}})
```bash
java -jar nuts-app-{{apiVersion}}.jar -Zy
```

On Windows systems, the first launch will create a new **nuts** menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal:
- **nuts-cmd-{{apiVersion}}**: Opens a configured command terminal. The **nuts** command will be available, as well as several companion tools installed by default.
- **nuts-cmd**: Points to the last installed **nuts** version (here, {{apiVersion}}).

Any of these shortcuts will launch a **nuts**-aware terminal. Supported systems include Windows 7 and later.

:::tip

Any of the created shortcuts for Windows is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="linux">

```bash
$ curl -s https://thevpc.net/nuts/install-latest.sh | bash
$ exit
```

Linux installation relies on the bash shell. The first launch configures `~/.bashrc` so that **nuts** and its companion tools are immediately available in future terminal sessions. 

Using **nuts** on a UNIX-like system is seamless. A standard bash terminal (GNOME Terminal, KDE Konsole, etc.) automatically becomes a **nuts**-aware terminal. A graphical system (X11/Wayland) is only required if you plan to run GUI applications via **nuts**.

:::tip

Any bash terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="macos">

```bash
$ curl -s https://thevpc.net/nuts/install-latest.sh | bash
$ exit
```

macOS installation leverages the `zsh` shell. The first launch configures `~/.zshrc` so that **nuts** and its companion commands are available in all future terminal instances. 

A standard terminal (like the macOS Terminal App) automatically acts as a **nuts**-aware terminal.

:::tip

Any terminal application is a nuts-aware terminal.

:::

</TabItem>
<TabItem value="wget">

```bash
$ wget -qO- https://thevpc.net/nuts/install-latest.sh | bash
$ exit
```

</TabItem>
<TabItem value="curl">

```bash
$ curl -s https://thevpc.net/nuts/install-latest.sh | bash
$ exit
```

</TabItem>
</Tabs>

You should then see log output resembling the following:

![install-log-example](assets/images/console/install-log-example.png)

As shown, the initial installation also triggers the setup of helpful optional programs called "companion tools." A highly recommended tool is **nsh** (Nuts Shell), a portable bash-compatible shell that runs uniformly across Linux, macOS, and Windows.

:::important

After installation, you must restart your terminal application for the environment configurations to take effect.

:::

## Test Installation

To verify your installation, open a new terminal and type:

```bash
nuts --version
```

It should return the version string in the format `nuts-api-version/nuts-impl-version`:

```bash
{{apiVersion}}/{{runtimeVersion}}
```

## Run a Command

To run a command using **nuts**, just type:

```bash
nuts <command>
```

You can seamlessly run **nuts** built-ins as well as any packaged Java CLI application.

## GUI Installer

Prefer a graphical installer? **nuts** provides native installers for Windows, macOS, and Linux.

[→ Download the GUI installer](/download.html)

The installer lets you easily choose:
- Stable or Preview channel
- Whether to reset your workspace
- Companion tools to pre-install
- Light or dark display themes

## Next Steps

Now that you have **nuts** installed, explore what you can do with it:
* Check out the [First App](/doc-nuts/01-intro/040-first-app.md) guide to install and run your first package.
* Dive into the [Tutorials](/doc-nuts/02-tutorial/) section for in-depth workflows.
