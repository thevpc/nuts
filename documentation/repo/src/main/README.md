# nuts
Network Updatable Things Services
<pre>
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )   latest version {{runtimeVersion}} 
\_\ \/\__,_/\__/____/    LTS version {{stableRuntimeVersion}} 
</pre>
Runtime package manager for Java applications.
No fat-jars. No manual JDK setup. Dependencies are shared and resolved at runtime.
Website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)
Docs: [Official documentation](https://thevpc.github.io/nuts/doc-nuts.html)

## Install (30 seconds)

```bash
curl -s https://thevpc.net/nuts/install-latest.sh | bash
```
>> Restart your terminal, then try:

```bash
nuts io.github.jiashunx:masker-flappybird
```

If it runs, Nuts is working.

## What is Nuts?

Unlike Maven, which resolves dependencies at compile time, ```nuts``` is the Java Package Manager that resolves them at install or runtime, saving disk space and bandwidth by caching only what’s needed for the current environment and sharing it across apps.

```nuts``` is fully compatible with existing Maven-built JARs—you don’t need to repackage your app or add custom metadata. If it’s in Maven Central (or any repo), you can run it with Nuts.

No more ugly lib folders or fat-jars. Just install and run in a single command.

## Why Nuts?
**For developers**
- Run tools with one command
- No setup or local dependency hell
- No fat-jars
- Shared dependency cache
- Automatic JVM selection
- Multiple versions side-by-side

**For operations and enterprise environments**
- Works offline and in air-gapped networks
- Local or mirrored repositories
- Per-repository permissions
- Signed operations and audit logs
- Reproducible, verifiable bundles

### Essential Commands

| Action      | Command                                 |
|:------------|-----------------------------------------|
| Install     | nuts install <group-id>:<artifact-id>   |
| Update      | nuts update <group-id>:<artifact-id>    |
| Run         | nuts  <group-id>:<artifact-id>          |
| Uninstall   | nuts uninstall <group-id>:<artifact-id> |
| Search      | nuts search <keyword>                   |
| Update Nuts | nuts update                             |

## Try Nuts (Cool Examples)


| Command                                                                                    | Description                                                         |
|--------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| nuts io.github.jiashunx:masker-flappybird                                                  | run game flappybird                                                 |
| nuts com.github.anuken:mindustry-desktop                                                   | run game mindustry                                                  |
| nuts com.github.anuken:mindustry-server                                                    | run game mindustry server                                           |
| nuts org.apache.netbeans:netbeans                                                          | run netbeans ide (latest version)                                   |
| nuts org.apache.catalina:apache-tomcat                                                     | run tomcat web server (latest version)                              |
| nuts org.postgresql:pgsql                                                                  | run postgresql database server                                      |
| nuts org.jmeld:jmeld                                                                       | run a folder diff tool                                              |
| nuts com.jgoodies:jdiskreport                                                              | run JDiskReport a tool to analyze disk space usage                  |
| nuts org.jd:jd-gui                                                                         | run a java decompiler tool                                          |
| nuts jpass:jpass                                                                           | run JPass a Password Manager                                        |
| nuts org.jedit:jedit                                                                       | run JEdit text editor tool                                          |
| nuts netbeans-launcher                                                                     | run a multi instance runner for netbeans                            |
| nuts kifkif                                                                                | run a file duplicates tool                                          |
| nuts pnote                                                                                 | run pnote a note taking tool                                        |
| nuts nops --gui                                                                            | run a developer friendly DevOps tool                                |
| nuts ntexup show                                                                           | run a Latex like Presentation Tool                                  |
| nuts com.github.todense:omnigraph                                                          | run Omnigraph Tool                                                  |
| nuts org.springframework.boot:spring-boot-cli  init --dependencies=web,data-jpa my-project | run spring boot client and create new project using spring-boot-cli |
| nuts com.google.tsunami:tsunami-main --ip-v4-target=127.0.0.1                              | run google tsunami a network security scanner tool                  |

### Under the Hood
- Nuts builds on a few core pieces:
- **[TSON](https://github.com/thevpc/tson) Reference Implementation** structured config format with expressions
- **Resilience Patterns Built-In** – retries, circuit breakers, rate limiting
- **Portable caches & bundles** – share or ship environments offline
- **Full Plugin Architecture** – SPI-based extension model with component lifecycle, dependency injection awareness, and repository-level security hooks for auditable customizations.

Deep details live in the documentation.

## Contribute
Open source. LGPL v3.
https://github.com/thevpc/nuts

