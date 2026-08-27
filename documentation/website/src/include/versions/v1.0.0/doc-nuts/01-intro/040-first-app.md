---
id: start_here
title: First App
---

## Let's Start the Journey

Get started by **running your first application**. 

Remember, **nuts** is not a build tool (like Maven or Gradle). Instead, think of it as the Java equivalent to Node's `npm` or Python's `pip`. It provides a seamless CLI experience to install, manage, and execute Java applications without dealing with classpath configurations or downloading JARs manually.

### What You'll Need

- A compatible operating system (Linux, macOS, or Windows).
- [Java](https://www.java.com) version 1.8 or above. A JRE is sufficient to run applications, though a JDK unlocks additional development features.

Verify your Java installation:

```bash
java -version
```

*Note: The following examples assume a Linux or macOS environment. If you are on Windows, refer to the [Installation](030-installation.md) page for exact commands.*

### Installing and Running Nuts

Open a new terminal and download **nuts**. We will use the direct JAR download method to demonstrate the standard initial setup:

```bash
curl -sL {{stableJarLocation}} -o nuts.jar && java -jar nuts.jar -Zy
```

We used the flags `-y` to automatically answer "yes" to prompts and `-z` to ignore cached binaries, ensuring a fresh installation. This process may take a minute as it fetches required core dependencies and companion tools.

Once finished, you should see a success message indicating **nuts** is installed. **Restart your terminal** to ensure your `PATH` is updated.

### Installing a GUI Application

Let's install `jedit`, a mature and feature-rich text editor. In your terminal, type:

```bash
nuts install org.jedit:jedit
```

Once installed, simply run it:

```bash
nuts jedit
```

**nuts** resolves the Maven dependencies, provisions the environment, and launches the application automatically.

### Installing a CLI Tool

**nuts** also excels at managing command-line tools. Let's install `nsh` (Nuts Shell), a portable bash-compatible shell:

```bash
nuts install net.thevpc.nuts.toolbox:nsh
```

Run it by typing:

```bash
nuts nsh
```

You are now inside the Nuts Shell. Type `exit` to return to your normal terminal.

### Managing Your Packages

You can easily see everything you have installed in your current workspace:

```bash
nuts search --installed
```

If you no longer need an application, you can uninstall it cleanly:

```bash
nuts uninstall org.jedit:jedit
```

### Next Steps

You've successfully installed **nuts**, provisioned a GUI application, ran a CLI tool, and managed your packages. To learn more advanced workflows:

* Explore the [Tutorials](../02-tutorial/) to dive deeper into workspace management, alias creation, and deployment.
* Visit the [How-To Guides](../03-how-to/) for practical, goal-oriented solutions.
