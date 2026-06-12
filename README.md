# nuts

```
     __        __
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   latest version 0.8.9.0 
\_\ \/\__,_/\__/____/    LTS version 0.8.9.0 
```

**The package manager Java never had.**

Website: https://thevpc.github.io/nuts  
Docs: [Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

---

![Nuts in action](documentation/term-cast/nuts-install-demo.webp)


## What is Nuts?
Nuts is a package manager for Java applications that focuses on the application lifecycle and isolated deployments.

Reusing Maven descriptors directly, Nuts manages dependencies at runtime, not at build time and solves the long-standing fat-jars problem efficiently:

It downloads only the JARs and dependencies that are actually needed on the target machine. Similarly, for native binaries, it retains only assets relevant for the target platform.

Nuts does not require any custom descriptors or build tools, does not change classloading behavior, it just solves dependency tree, builds the classpath and runs the application. 

What makes nuts unique is that it shares the same workspace across all applications, enables installing multiple versions of the same app, and automatically provisions the required platform binaries (JDK).
A simple `nuts install myapp` is all what one needs to install the latest version of myapp and all its dependencies including the JDK while optimizing network and disk usage.

Think of nuts as `npm`/`nvm`, or `uv`, but for the java ecosystem.

---

## Installation

### Latest (recommended)

```bash
curl -s https://thevpc.net/nuts/install-latest.sh | bash
```

Restart your terminal, then verify:

```bash
nuts --version
```

### Stable (for production)

```bash
curl -s https://thevpc.net/nuts/install-stable.sh | bash
```

### Update existing installation

```bash
nuts update
```

---

## Key Features
- Works with existing Maven JARs — zero repackaging
- Supports multiple repositories simultaneously, including Maven-central and Maven-compatible repositories
- Full application lifecycle — Install, run, update, and uninstall applications — with multiple versions coexisting in the same workspace
- Filesystem isolation — Everything is isolated in a workspace compliant to the system standards (XDG for Linux, etc...)
- JDK provisioning — resolve required java version for each application and automatic provisioning
- Not just Java — can manage any application, as long as a compliant descriptor is provided

## Quick Examples

| Command                                                                        | What it does                                                  |
|--------------------------------------------------------------------------------|---------------------------------------------------------------|
| `nuts org.apache.netbeans:netbeans`                                            | install and run NetBeans IDE                                  |
| `nuts org.springframework.boot:spring-boot-cli init --dependencies=web my-app` | scaffold a Spring Boot project                                |
| `nuts jd-gui`                                                                  | decompile a JAR                                               |
| `nuts uninstall jd-gui#1.6.6`                                                  | uninstall version jd-gui version 1.6.6                        |
| `nuts update nsh`                                                              | update to to the newest version of java bash compatible shell |
| `nuts org.postgresql:postgresql-server initdb -D ~/pgdata`                     | initialize a portable PostgreSQL instance (not a java app)    |
| `nuts --at=ssh://me@myserver install myapp:2.0`                                | deploy to remote server                                       |
| `nuts settings bundle`                                                         | create offline deployment bundle (air-gapped deployment)      |
| `nuts search --installed --json`                                               | list installed apps as JSON                                   |
| `nuts myapp#1.0.0`                                                             | run a specific installed version                              |
| `nuts nsh -c "cp http://host/a.pdf ssh://user@server/b.pdf"`                   | cross-protocol file copy                                      |
| `nuts settings install-service myapp`                                          | Register system service across all major init systems         |

→ [See the full showcase](SHOWCASE.md) for more examples including databases, IDEs, security tools, and games.

---

## How Nuts relates to other tools

| Tool | What it does | Relationship to Nuts |
|---|---|---|
| Maven / Gradle | Compile-time dependency resolution | Nuts reuses their descriptors at runtime |
| jbang | Run Java scripts and JARs | Nuts adds workspaces, platforms, lifecycle, deployment |
| sdkman / jenv | Manage JDK versions | Nuts subsumes this as part of workspace platform management |
| Docker | Isolated application environments | Nuts is lighter — no daemon, no root, no image layers |
| apt / brew / snap / flatpak | System or per-app package management | Nuts is per-workspace with dependencies shared across apps in the same workspace (no per-app bundling), no root required, and portable across Linux, Windows, and macOS |
| Ansible | Infrastructure and configuration management | Complementary — Nuts handles the Java app lifecycle layer |
| jgo | Launch Java apps from Maven coordinates (Python-based) | Similar launch model; Nuts is Java-native with workspaces, lifecycle, and deployment |

Nuts fills the space between "compile-time dependency management" and "production deployment" that the Java ecosystem has never had a clean answer for.

---

## Contribute

Nuts is open-source and actively developed. Contributions welcome.

[Contribute on GitHub](https://github.com/thevpc/nuts)  
[Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

---

## License

Licensed under the GNU Lesser General Public License v3.