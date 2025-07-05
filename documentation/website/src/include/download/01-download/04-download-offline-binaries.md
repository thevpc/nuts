---
id: downloadAppFull
title: Offline Binaries
---

Offline binaries bundle the Nuts bootstrapper together with its runtime, eliminating the need for internet access during the first execution.  
These distributions are ideal for **offline environments**, **Docker images**, and **fast installations** in controlled setups.

:::tip
**Note:** A valid Java Runtime Environment (JRE) version **1.8 or higher** is required unless otherwise noted. Nuts is fully compatible with the latest **Java 24** release.
:::

| Platform       | Package Type                        | Description                                                                                         | Download Link                |
|----------------|--------------------------------------|-----------------------------------------------------------------------------------------------------|------------------------------|
| **All OS**     | Java Offline Binaries                | Cross-platform archive including bootstrapper and runtime. Requires Java 8+ installed.             | [Portable Offline Binaries ({{fileContentLengthString($'$localDist/nuts-app-full-$runtimeVersion.jar')}})](https://thevpc.net/nuts/nuts-app-full-{{runtimeVersion}}.jar) |

| **Linux**      | Linux x64 Offline Binaries           | Native offline package. Requires Java 8+ already installed.                                        | [Linux x64 Offline Binaries ({{fileContentLengthString($'$localDist/nuts-app-full-linux64-bin-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-linux-x64-{{runtimeVersion}}.zip)        |
|                | Linux x64 Offline Binaries + JRE     | Bundled with compatible JRE. Does not require pre-installed Java.                                  | [Linux x64 Offline Binaries + JRE ({{fileContentLengthString($'$localDist/nuts-app-full-linux64-bin-with-java-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-linux64-bin-with-java-{{runtimeVersion}}.zip)        |
|                | RedHat/OpenSuSE Offline RPM          | RPM package with all dependencies for RPM-based distros (RedHat, Fedora, OpenSuSE, etc.).          | [Redhat/OpenSuSE Linux x64 Offline RPM ({{fileContentLengthString($'$localDist/nuts-app-full-linux64-rpm-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-linux64-rpm-{{runtimeVersion}}.rpm)    |

| **Windows**    | Windows x64 Offline Binaries         | Native offline package. Requires Java 8+ already installed.                                        | [Win x64 Offline Binaries ({{fileContentLengthString($'$localDist/nuts-app-full-windows64-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-windows64-{{runtimeVersion}}.exe)        |
|                | Windows x64 Offline Binaries + JRE   | Bundled with JRE. Does not require pre-installed Java.                                              |[Win x64 Offline Binaries + JRE ({{fileContentLengthString($'$localDist/nuts-app-full-windows64-with-java-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-windows64-with-java-{{runtimeVersion}}.zip)      |

| **macOS**      | macOS x64 Offline Binaries           | Native offline archive for macOS. Requires Java 8+ already installed.                              | [MacOS x64 Offline Binaries ({{fileContentLengthString($'$localDist/nuts-app-full-mac64-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-app-full-mac64-{{runtimeVersion}}.app.zip)       |



