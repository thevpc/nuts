---
id: whatIsNuts
title: What is Nuts?
---

**nuts** (Network Updatable Things Services) is a portable runtime package manager for Java and the JVM. If you publish to Maven Central, your application is already a **nuts** package—no special registry or packaging format required.

## The Problem

For years, Java has lacked a true runtime package manager. To distribute an application, developers typically build "fat JARs" (shadow JARs) containing all dependencies. This approach has significant drawbacks:
* **Bloat**: Bundling dependencies inflates file sizes, wasting disk space and network bandwidth.
* **No side-by-side versioning**: Running multiple versions of the same tool locally often involves brittle scripts or environment variable juggling.
* **Complex runtime setup**: Users must manually ensure the correct JDK version is installed before they can run a Java application.
* **Poor CLI experience**: There is no simple `install and run` workflow native to the ecosystem.

## The Solution

**nuts** shifts dependency resolution from build-time to runtime. It natively understands standard Maven POM descriptors, dynamically fetching and resolving required dependencies precisely when an application is executed. 

```bash
nuts install org.example:myapp
nuts myapp
```

## Key Differentiators

* **Zero External Dependencies**: The **nuts** bootstrap JAR is entirely self-contained.
* **Cross-Platform**: Operates with identical behavior across Linux, macOS, and Windows.
* **Workspace Isolation**: Supports isolated environments, conceptually similar to Python's `virtualenv`.
* **JDK Auto-Provisioning**: Automatically detects, downloads, and configures the appropriate JDK version (and vendor) required by the application.
* **Structured Output**: Built-in support for generating output in JSON, YAML, XML, TSON, and plain text formats.
* **Self-Hosting**: **nuts** manages itself—using the same mechanisms to install, update, and uninstall its own binaries.

## What Nuts is NOT

* **Not a build tool**: You still use Maven, Gradle, or Ant to compile and package your code. **nuts** is meant to install, update, and run the resulting artifacts.
* **Not a replacement for Maven Central**: **nuts** seamlessly consumes artifacts from Maven Central and other standard repositories rather than replacing them.

## Nuts Application Framework (NAF)

**nuts** is built upon the [Nuts Application Framework (NAF)](/doc-naf), a robust foundation for building feature-rich applications. If you are developing apps that deeply integrate with **nuts** or want to leverage its underlying services, refer to the NAF documentation.
