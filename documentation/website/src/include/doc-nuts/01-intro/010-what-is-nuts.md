---
id: whatIsNuts
title: What is Nuts?
---

Nuts is a package manager for Java applications. If you publish to Maven Central, your app is already installable.

```bash
nuts install org.example:myapp
nuts myapp
```

It handles dependencies at install time (no fat jars), supports multiple versions side-by-side, and can provision the correct JDK automatically.
Not a build tool. Use Maven or Gradle to build. Use Nuts to install, update, and run.



Nuts is built on the [Nuts Application Framework (NAF)](/doc-naf).
If you are building apps that integrate with Nuts, see the NAF docs.
