---
id: start_here
title: First App
---

Now that **nuts** is installed on your system, let's walk through installing, running, and managing your first applications.

*Note: If you have not installed **nuts** yet, see the [Installation Guide](020-installation.md) or visit the [Download Page](/download.html).*

## 1. Running a GUI Application

Let's install `jedit`, a popular open-source text editor built for Java.

Install `jedit` in your workspace:

```bash
nuts install org.jedit:jedit
```

Once installed, launch it directly using its application name:

```bash
nuts jedit
```

**nuts** automatically resolves the required dependencies, provisions the execution environment, and launches the application window.

## 2. Running a CLI Tool

**nuts** manages command-line tools just as effortlessly as desktop apps. Let's install `nsh` (Nuts Shell), a portable bash-compatible shell environment:

```bash
nuts install net.thevpc.nuts.toolbox:nsh
```

Launch the shell:

```bash
nuts nsh
```

You are now inside the Nuts Shell session. Type `exit` to return to your standard terminal.

## 3. Managing Packages

### List Installed Packages
To view all packages installed in your current workspace:

```bash
nuts search --installed
```

### Search for Packages
To discover packages available in remote repositories:

```bash
nuts search netbeans
```

### Update Packages
To check for and apply updates across all installed packages:

```bash
nuts update
```

### Uninstall a Package
To cleanly remove a package when it is no longer needed:

```bash
nuts uninstall org.jedit:jedit
```

## Next Steps

Congratulations! You've learned how to install, launch, and manage applications with **nuts**.

* Explore the [Tutorials](/doc-nuts/02-tutorial/) for in-depth guides on workspace configuration, aliases, and custom repositories.
* Check out the [How-To Guides](/doc-nuts/03-how-to/) for practical solution recipes.
