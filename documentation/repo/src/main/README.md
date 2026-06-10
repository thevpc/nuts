# nuts

```
     __        __
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   latest version {{runtimeVersion}} 
\_\ \/\__,_/\__/____/    LTS version {{stableRuntimeVersion}} 
```

**The package manager Java never had.**

Website: https://thevpc.github.io/nuts  
Docs: [Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

---

![Nuts in action](documentation/term-cast/nuts-install-demo.webp)


## What is Nuts?

Every major language has a package manager that lets you *run* things, not just build them.
Python has `pip`, Node has `npm`, Rust has `cargo`.
Java has Maven — but Maven resolves dependencies at *compile time*, for *developers*.
There has never been a tool that lets you install and run Java applications the way `pip install` or `npx` does.

**Nuts fills that gap.**

```bash
nuts cowsay "Hello!"
nuts netbeans
nuts apache-tomcat
nuts spring-boot-cli init --dependencies=web,data-jpa my-project
```

No fat-jars. No copying lib folders. No `java -cp ...` incantations.
Just install and run — like every other ecosystem has done for years.

And because Nuts manages dependencies at runtime rather than bundling them,
**only what changed gets transferred on each update** — not a 500MB uber-jar every time.
Change one class in one module, deploy that module. Everything already cached on the target stays put.

---

## But Nuts is more than a launcher

Most package managers install things *onto your system*. Nuts installs things *into a workspace* —
an isolated, portable, reproducible environment scoped to you, your project, or your server.
No root required. No system pollution. No conflicts between versions.

Unlike containers, there is no daemon, no image layer complexity, no networking overhead.
Just your application, running as your user, with only what it needs.

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

### Works with existing Maven JARs — zero repackaging

Nuts reuses standard `pom.xml` descriptors directly.
No custom metadata, no special packaging format, no changes to your build.
If your artifact is on Maven Central (or any Maven-compatible repository), Nuts can install and run it today:

```bash
nuts install com.mycompany:myapp:1.0.0
nuts myapp
```

Custom Nuts descriptors are supported for advanced cases, but never required.

### Full application lifecycle

Install, run, update, and uninstall applications — with multiple versions coexisting in the same workspace:

```bash
nuts install myapp:1.0.0          # install version 1.0.0
nuts install myapp:2.0.0          # install 2.0.0 alongside it
nuts myapp                        # run default version
nuts myapp#1.0.0                  # run specific version explicitly
nuts update myapp                 # upgrade to latest
nuts uninstall myapp:1.0.0        # remove a specific version
```

No version conflicts. No "this tool requires X but that tool requires Y."
Each application resolves exactly what it needs from the shared workspace cache.

### Platforms, not just packages

Nuts understands that Java itself is not a package — it is a *platform*.
JDK provisioning is a first-class operation:

```bash
nuts settings add java#17        # provision JDK 17 into the workspace
nuts settings add java#21        # coexist with JDK 21
nuts --java=21 myapp             # override JVM per invocation
```

Each application selects the minimum compatible JVM it needs, read directly from its `pom.xml`.
No global "active" version. No `.java-version` files to forget to update.
GraalVM, Corretto, Zulu — all are alternate providers of the same `java` platform.

### Not just Java

Nuts manages any distributable application, not just JARs.
For non-Java tools, the Toolbox repository bridges Maven coordinates
to native binary distributions — resolving the right binary for your OS and architecture,
downloading, extracting, and creating wrapper scripts, all inside your workspace:

```bash
nuts install org.apache.netbeans:netbeans        # NetBeans IDE
nuts install org.apache.catalina:apache-tomcat   # Tomcat web server
nuts install org.apache.maven:mvn                # Maven itself
nuts install org.postgresql:postgresql-server    # portable PostgreSQL
```

Run a local PostgreSQL instance without touching your system:

```bash
PGDATA=~/pgdata
nuts org.postgresql:postgresql-server initdb -D $PGDATA
echo "port = 8666" >> ~/pgdata/postgresql.conf
nuts org.postgresql:postgresql-server pg_ctl -D ~/pgdata -l ~/pgdata/logfile start
```

No `sudo`. No `/etc/postgresql`. No conflict with whatever the sysadmin already installed.

### Multi-repository, Maven-compatible

Nuts supports multiple repositories simultaneously, each with its own layout and protocol:

```bash
nuts settings list repos
# [x] maven-central   https://repo.maven.apache.org/maven2
# [x] maven-local     ~/.m2/repository
# [x] local           ~/.nuts/.../local
# [x] toolbox         ~/.nuts/.../toolbox
# [x] my-nexus        https://nexus.mycompany.com/repository/releases
```

Maven Central is supported out of the box. Any Maven-compatible repository
(Nexus, Artifactory, GitHub Packages, raw GitHub via `dotfilefs`) works without configuration.
Proxy/cache repositories reduce bandwidth by caching remote artifacts locally
and sharing them across all applications in the workspace.

### Workspace isolation

Everything lives inside a workspace — an XDG-compliant directory tree
with dedicated locations for each type of data:

```
BIN    → platform binaries (JDKs, native runtimes)
LIB    → application libraries and JARs
CONF   → configuration files
CACHE  → downloaded artifacts (shared and reused across all apps)
LOG    → log files
RUN    → sockets and runtime files
```

Multiple workspaces coexist on the same machine.
Workspaces support `--confined`, `--sandbox`, and `--isolation` modes for stricter control.
The `standalone` store strategy collapses all trees into one folder — useful for portable USB deployments.

### Differential deployment

Traditional Spring Boot deployment pushes a 500MB uber-jar on every release,
even when you changed one class in one 5KB module.

Nuts deploys only what changed:

```bash
nuts install myapp:2.1.4         # transfers only the updated JAR + descriptor update
```

Dependencies already cached in the remote workspace stay put.
Over a slow pipe, deploying to 10 servers 50 times a day,
this is the difference between minutes and seconds.

### Remote workspace management

Manage remote servers with the same commands you use locally:

```bash
nuts --at=ssh://user@192.168.1.1 install myapp:2.1.4
nuts --at=ssh://user@192.168.1.1 search --installed --json
nuts --at=ssh://user@192.168.1.1 myapp stop
nuts --at=ssh://user@192.168.1.1 myapp start
```

No Ansible playbook for the application lifecycle layer.
No SSH session scripting. The same mental model, local or remote.

### Offline / air-gapped deployment

For servers with no internet access:

```bash
nuts settings bundle
```

Creates a self-extracting archive — a fat-jar-of-jars — that includes
all JARs, platform binaries, and workspace configuration.
Drop it on the target machine and extract. No internet required, no prerequisites beyond a compatible JVM.

Used in production for government and enterprise environments where the deployment
target cannot reach the internet.

### Service management

Register, start, stop, and uninstall system services across all major init systems:

```bash
nuts settings install-service myapp
nuts settings uninstall-service myapp
```

Nuts generates start/stop/status scripts and native unit files
for systemd, SysV init, and initd — whichever the target system uses.

### nsh — a portable shell with structured output

Nuts ships `nsh`, a bash-compatible shell implemented in pure Java,
with first-class support for structured output formats:

```bash
nuts nsh -c "ls --json"
nuts nsh -c "ps --table"
nuts nsh -c "find . -name '*.java' --xml"
nuts nsh -c "ls --yaml"
```

Useful for scripting in Java-only environments, CI pipelines,
or anywhere you need machine-readable output from shell commands without additional tooling.

### A unified path model across every protocol

Everything in Nuts — nsh, package resolution, content fetching, deployment — is powered
by a single runtime path abstraction (`NPath`) that treats all protocols uniformly.
This is not shell magic. It is the same API available to every application running inside Nuts.

```bash
# copy from HTTP directly to a remote server over SSH
nuts nsh -c "cp http://files.example.com/app.jar ssh://user@prod-server/deploys/"

# tail a remote log file
nuts nsh -c "cat ssh://user@myserver/var/log/myapp.log | grep ERROR"

# copy between two remote servers without a local intermediate
nuts nsh -c "cp ssh://user@server1/data/dump.sql ssh://user@server2/restore/dump.sql"
```

Supported protocols: `http`, `https`, `ssh`, `htmlfs` (navigable HTTP directory listings),
`dotfilefs` (GitHub raw content trees), and local paths — all composable, all interchangeable.

Because this is the runtime, not the shell, every Nuts-aware tool inherits it automatically.

---

## Quick Examples

| Command | What it does |
|---|---|
| `nuts cowsay "hi"` | install and run cowsay |
| `nuts netbeans` | install and run NetBeans IDE |
| `nuts apache-tomcat` | install and run Tomcat |
| `nuts spring-boot-cli init --dependencies=web my-app` | scaffold a Spring Boot project |
| `nuts jd-gui MyApp.jar` | decompile a JAR |
| `nuts kifkif` | find duplicate files |
| `nuts org.postgresql:postgresql-server initdb -D ~/pgdata` | initialize a portable PostgreSQL instance |
| `nuts --at=ssh://me@myserver install myapp:2.0` | deploy to remote server |
| `nuts settings bundle` | create offline deployment bundle |
| `nuts search --installed --json` | list installed apps as JSON |
| `nuts myapp#1.0.0` | run a specific installed version |
| `nuts nsh -c "cp http://host/a.pdf ssh://user@server/b.pdf"` | cross-protocol file copy |

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

Nuts does not try to replace any of these. It fills the space between
"compile-time dependency management" and "production deployment"
that the Java ecosystem has never had a clean answer for.

---

## Architecture in brief

Nuts workspaces follow the XDG Base Directory Specification on Linux
and equivalent conventions on Windows and macOS. The store type system
(`BIN`, `LIB`, `CONF`, `CACHE`, `LOG`, `TEMP`, `RUN`) maps directly to OS filesystem conventions,
giving every application a well-structured, predictable home.

**Platforms vs packages** — Platforms (JDK, Node, Python runtimes) are managed separately from packages
(JARs, applications). A platform is provisioned into `BIN`; packages resolve into `LIB`/`CACHE`.
Applications declare their platform requirements in their `pom.xml`;
Nuts provisions them automatically into the workspace, selecting the smallest compatible version.

**No custom descriptors required** — Nuts reads standard `pom.xml` metadata directly,
including compiler target version for JVM compatibility resolution.
Custom Nuts descriptors extend this for non-Java artifacts but are never required for Maven-published JARs.

**Multi-repository** — Nuts manages a set of repositories per workspace.
Maven Central is built in. Any Maven-compatible repository works out of the box.
Repository types include local, remote Maven, proxy/cache, and Toolbox
(which bridges Maven coordinates to native binary distributions for PostgreSQL, NetBeans, Tomcat, and others).
Multiple repository layouts are supported: exploded (XDG-style separate trees)
and standalone (single self-contained folder, useful for portable and air-gapped deployments).

**Maven immutability** — Because Nuts builds on Maven repositories,
it inherits Maven's immutability contract: a published version is sealed forever.
No mutable tags, no surprise re-deployments. The same command run twice always produces the same result.

---

## Contribute

Nuts is open-source and actively developed. Contributions welcome.

[Contribute on GitHub](https://github.com/thevpc/nuts)  
[Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

---

## License

Licensed under the GNU Lesser General Public License v3.