---
title: Provision JDK
---

## Provision a JDK

One of the most powerful features of **nuts** is its ability to manage its own runtime environment. In traditional Java development, you must manually install, configure, and switch between different JDK versions across projects. With **nuts**, you can auto-provision JDKs on demand, ensuring your applications always run with the correct Java version without manual intervention.

### Auto-detect Local JDKs

If you already have Java installed on your system, you can instruct **nuts** to scan your local machine and register any existing JDK installations. This allows **nuts** to utilize your existing toolchains:

```bash
nuts settings add java --search
```

### Download a Specific JDK

When an application requires a Java version that is not installed on your system, you can provision it directly. **nuts** will download the JDK, verify it, and register it in the workspace:

```bash
nuts settings add java --download --jdk --version=21
```

### Download Only JRE (Smaller Footprint)

If you only need to run Java applications and do not require compilation tools (like `javac`), you can provision a Java Runtime Environment (JRE). This significantly reduces download size and disk footprint:

```bash
nuts settings add java --download --jre --version=21
```

### Select a Specific Vendor

By default, **nuts** provisions Java from the Eclipse Temurin (Adoptium) distribution. However, the provisioning system is SPI-extensible, allowing you to choose your preferred vendor:

```bash
nuts settings add java --download --vendor=temurin --version=21
```

#### Supported Vendors

The following vendors are commonly supported for auto-provisioning:

| Vendor ID | Distribution | Notes |
|-----------|--------------|-------|
| `temurin` | Eclipse Temurin (Adoptium) | Default open-source distribution. Highly recommended. |
| `oracle` | Oracle JDK | Official Oracle distribution. |
| `graalvm` | GraalVM | High-performance JDK with AOT compilation capabilities. |
| `zulu` | Azul Zulu | Certified builds of OpenJDK. |
| `corretto` | Amazon Corretto | No-cost, multiplatform, production-ready distribution. |

### Using a Specific JDK for Execution

Once your JDKs are provisioned (or even if they aren't yet), you can instruct **nuts** to execute an application using a specific Java version. If the requested version is not currently available in the workspace, **nuts** will automatically attempt to provision it before running the app:

```bash
nuts exec --java-version=11 my-app
```

### Listing Available JDKs

To view all JDKs currently registered in your **nuts** workspace, use the settings command. This will output a list of available environments, their versions, and their local paths:

```bash
nuts settings list java
```
