---
id: running
title: Running Nuts
sidebar_label: Running Nuts
---

:::tip What You'll Learn
In this section, you will learn the fundamentals of using the **nuts** package manager to run Java applications. We will cover:
* Running remote and deployed artifacts seamlessly.
* Understanding and using artifact long IDs (Maven coordinates).
* Installing applications before running them.
* Searching your workspace for installed artifacts.
* Executing local JAR files with auto-provisioned dependencies.
* Using different execution modes (spawn, embedded, system).
:::

In this section, we will explore the core usage of the **nuts** package manager: running applications. Because **nuts** is designed to act as a runtime package manager for Java, it handles dependencies, classpaths, and JVM arguments dynamically.

## Running a Deployed Artifact

You can run any JAR using **nuts**, provided the JAR is accessible from one of your configured repositories. By default, **nuts** supports:
* Maven Central
* Local Maven folder (`~/.m2/repository`)

You can also configure additional repositories (like Nexus or Artifactory) or implement your own if you need to. 

When you run an artifact, the JAR is parsed to check its Maven descriptor (`pom.xml` properties). **nuts** will resolve and download all necessary dependencies on the fly. After resolving dependencies, all executable classes (classes containing a `public static void main` method) are enumerated. If there are multiple entry points, you can choose which one to run when prompted. 

Any standard JAR built with Maven or Gradle can be executed by referencing its artifact ID.

## Artifact Long IDs

**nuts** uses "Long IDs" to uniquely identify artifacts. These strings follow the standard Maven coordinate format:

```bash
groupId:artifactId#version
```

For instance, to install and run `netbeans-launcher` (a simple UI utility that helps launch multiple instances of NetBeans), you can execute:

```bash
nuts net.thevpc.app:netbeans-launcher#1.2.2
```

Typing out the full `groupId` and `version` every time can be cumbersome. To simplify this, you can omit them when running commands:

```bash
nuts netbeans-launcher
```

When you use this short form, **nuts** auto-detects both the `groupId` and the `version`. The group ID is resolved if it matches an **imported** prefix (we will cover imports in detail later). By default, several group IDs are automatically imported:

* `net.thevpc` (contains various core applications)
* `net.thevpc.nuts.toolbox` (contains companion tools like `nsh`, `ndb`, etc.)

Because `netbeans-launcher` belongs to an imported group ID, the prefix can be safely omitted. Additionally, if no version is provided, **nuts** automatically selects the best version to execute. If you already have one or more versions installed, the default installed version will be used. If you have not installed it yet, the most recent stable version will be resolved and fetched for you.

## Artifact Installation

Any Java application can run using **nuts**, but it must be downloaded and installed first. If you attempt to run an application that is not yet installed in your local workspace, you will be prompted to confirm the installation.

To explicitly install an application without immediately running it, use the `install` command:

```bash
nuts install netbeans-launcher
```

If you try to run the application directly via `nuts netbeans-launcher`, the installation happens automatically (after you confirm the prompt).

## Searching Artifacts

To view the artifacts currently installed in your workspace, use the `search` command:

```bash
nuts search --installed
```

This lists all installed artifacts. For a more detailed view, you can use the long format flag `-l`:

```bash
nuts search --installed -l
```

The output will look similar to this:

```text
I-X 2024-03-15 14:30:22 anonymous vpc-public-maven net.thevpc.app:netbeans-launcher#1.2.0
i-X 2024-03-15 14:28:05 anonymous vpc-public-maven net.thevpc.app:netbeans-launcher#1.2.2
```

Here is how to interpret the output columns:
* **Status Flags (Column 1)**: The first column provides compact status information. 
  * `I` (uppercase) means "installed and default".
  * `i` (lowercase) means "installed".
  * `X` (uppercase) stands for "executable application aware of nuts" (meaning it uses the nuts API for features like `OnInstall` or `OnUninstall` hooks).
  * `x` (lowercase) simply means "executable" (a standard Java application with a main method).
* **Date and Time (Columns 2 & 3)**: When the artifact was installed.
* **User (Column 4)**: The user who performed the installation. If secure mode is disabled (the default), this shows as `anonymous`.
* **Repository (Column 5)**: The source repository from which the package was fetched.
* **Long ID (Column 6)**: The full artifact identifier.

## Running Local JAR Files

Let's suppose you have a file named `my-app.jar` built with Maven. Even if it is just a local file, **nuts** is capable of reading its embedded `META-INF/maven` files, resolving its external dependencies on the fly, and running it.

If a `Main-Class` attribute is present in a valid `MANIFEST.MF`, it will be executed. If multiple classes have a `main` method and no primary class is specified, **nuts** will list them and ask which one you want to run.

To run a local file, simply provide the path (which must contain a `/` or `\` to be recognized as a file path rather than an artifact ID):

```bash
nuts ./my-app.jar some-argument-of-my-app
```

Dependencies defined in the JAR's internal POM will be downloaded and cached in your workspace automatically.

### Passing JVM Arguments

If you need to pass JVM arguments (such as memory limits or system properties), you must prefix them with the `exec` command. For instance, to set the initial and maximum heap size:

```bash
nuts exec -Xms1G -Xmx2G ./my-app.jar argument-1 argument-2
```

You can also use this syntax to dynamically provision and select a specific Java version for the execution:

```bash
nuts exec --java-version=17 ./my-app.jar
```

## Execution Modes

When running applications, **nuts** supports different execution modes to control how the process is launched. These can be specified using flags:

* `--spawn` (Default): Launches the application in a completely new JVM process. This provides maximum isolation and ensures the application's environment variables and memory space are separate from the **nuts** process itself.
* `--embedded`: Runs the application within the same JVM process as **nuts**. This is faster since it avoids the overhead of starting a new JVM, but it provides less isolation. It is useful for trusted plugins or small utilities.
* `--system`: Delegates the execution directly to the underlying operating system. This is typically used when running native commands or system scripts rather than Java applications.

For example, to force an application to run in embedded mode:

```bash
nuts --embedded netbeans-launcher
```
