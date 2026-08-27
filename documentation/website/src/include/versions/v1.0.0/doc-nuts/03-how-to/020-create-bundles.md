---
title: Create Bundles
---

## Create an Air-Gapped Bundle

A **bundle** is a self-contained, highly portable package of a **nuts** workspace that includes an application and all its dependencies. Bundles are designed to run on machines without internet access or an existing **nuts** installation, completely solving the "fat-JAR" problem while maintaining isolation.

### Creating a Bundle

To create a bundle for an application, use the `nuts bundle` command, specifying the artifact ID and the desired output file:

```bash
nuts bundle myapp#1.2.3 --output myapp-bundle.jar
```

### What's Inside a Bundle?

When you create a bundle, **nuts** packages everything required to execute the application into a single executable JAR file:
* A minimal embedded **nuts** bootstrap engine
* The application artifact itself
* All transitive dependencies resolved at bundle-time
* Relevant workspace configuration

### Running a Bundle on the Target Machine

Deploying a bundle requires nothing more than a standard Java installation on the target machine. You do not need to install **nuts**, configure repositories, or have an internet connection.

Simply execute the bundle like any standard JAR:

```bash
java -jar myapp-bundle.jar
```

When executed, the bundle automatically:
1. Recreates an isolated **nuts** workspace in memory or temporary storage
2. Installs the embedded artifacts directly from the archive
3. Bootstraps and runs the application transparently

### Use Cases

Bundles are incredibly versatile and solve several complex deployment scenarios:
* **Air-gapped Servers**: Deploying software to secure environments with strict network isolation and no internet access.
* **Security-Restricted Environments**: Running applications where external dependency resolution is blocked by enterprise firewalls.
* **Portable Distribution**: Distributing a single file to clients without requiring them to understand package management.
* **Offline Demos**: Ensuring your software demonstrations run flawlessly without relying on venue Wi-Fi.

### Bundle with JRE

If the target machine does not have a compatible Java version installed, you can include a complete JRE directly within the bundle. This ensures 100% portability at the cost of a larger file size:

```bash
nuts bundle myapp#1.2.3 --output myapp-standalone.jar --include-jre
```

### Bundle Options

You can customize the bundling process using various options to optimize size and behavior:

* `--exclude-dependencies`: Exclude certain dependencies if you know they exist on the target system.
* `--minimize`: Apply compression and stripping techniques to reduce the final bundle size.