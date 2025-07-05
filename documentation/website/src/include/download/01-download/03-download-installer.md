---
id: downloadInstaller
title: Graphical Installers
---

Download Installer for your Operating System.
{{: localDist=$'$root/installers/nuts-release-tool/dist' }}

Download the appropriate installer for your operating system. Graphical installers are available for major platforms and provide a simple setup wizard.
> **Note:** A valid Java Runtime Environment (JRE) version **1.8 or higher** is required, and Nuts is compatible with the latest Java 24 release.

| Platform       | Installer Type | Description                                                                                      | Download Link                |
|----------------|----------------|--------------------------------------------------------------------------------------------------|------------------------------|
| **All OS**     | Java Installer | Cross-platform graphical installer. Double-click to run and follow the wizard.                  | [Portable Installer ({{fileContentLengthString($'$localDist/nuts-installer-$runtimeVersion.jar')}})](https://thevpc.net/nuts/nuts-installer-{{runtimeVersion}}.jar) |
| **All OS**     | No Installer   | Lightweight version (173KB). Requires existing Java 8+ installation.                            | [Download Portable]({{latestJarLocation}}) |

| **Linux**      | Linux x64 Installer             | Native installer. Requires existing Java 8+ installation.                                       | [Linux x64 Installer ({{fileContentLengthString($'$localDist/nuts-installer-linux64-bin-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-linux-x64-{{runtimeVersion}}.zip)        |
|                | Linux x64 Installer + JRE       | Native installer bundled with JRE. No pre-installed Java required.                             | [Linux x64 Installer + JRE ({{fileContentLengthString($'$localDist/nuts-installer-linux64-bin-with-java-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-linux64-bin-with-java-{{runtimeVersion}}.zip)        |
|                | RedHat / OpenSuSE RPM           | RPM package for RPM-based distros (RedHat, Fedora, OpenSuSE, etc.).                             | [Redhat/OpenSuSE Linux x64 RPM ({{fileContentLengthString($'$localDist/nuts-installer-linux64-rpm-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-linux64-rpm-{{runtimeVersion}}.rpm)    |

| **Windows**    | Windows x64 Installer           | Native installer. Requires existing Java 8+ installation.                                       | [Win x64 Installer ({{fileContentLengthString($'$localDist/nuts-installer-windows64-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-windows64-{{runtimeVersion}}.exe)        |
|                | Windows x64 Installer + JRE     | Installer bundled with JRE. No pre-installed Java required.                                    | [Win x64 Installer + JRE ({{fileContentLengthString($'$localDist/nuts-installer-windows64-with-java-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-windows64-with-java-{{runtimeVersion}}.zip)        |

| **macOS**      | macOS x64 Installer             | Native installer for macOS. Requires existing Java 8+ installation.                             | [MacOS x64 Installer ({{fileContentLengthString($'$localDist/nuts-installer-mac64-$runtimeVersion.zip')}})](https://thevpc.net/nuts/nuts-installer-mac64-{{runtimeVersion}}.app.zip)        |



