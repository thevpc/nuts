# Nuts Showcase

> All commands below require only `nuts` to be installed.
> Dependencies, JVM versions, and platform binaries are resolved and provisioned automatically.
> If a required JVM version is not found, nuts will download and install it into the workspace transparently.

---

## IDEs & Editors

| Command | Description |
|---|---|
| `nuts org.apache.netbeans:netbeans` | NetBeans IDE (latest version) |
| `nuts netbeans-launcher` | Multi-instance NetBeans runner |
| `nuts org.jedit:jedit` | JEdit text editor |

---

## Web & Application Servers

| Command | Description |
|---|---|
| `nuts org.apache.catalina:apache-tomcat` | Tomcat web server (latest version) |
| `nuts org.eclipse.jetty:jetty-runner` | Jetty web server (latest version) |

### Jetty — serve a WAR file

```bash
curl -sL https://repo1.maven.org/maven2/com/example/simple-servlet/1.0/simple-servlet-1.0.war -o demo.war
nuts -yb --exec --class=1 org.eclipse.jetty:jetty-runner --port 8080 demo.war
```

Jetty and all its dependencies are resolved and installed automatically.
Any WAR file can be passed directly — not just the demo above.

---

## Databases

| Command | Description |
|---|---|
| `nuts org.postgresql:postgresql-server` | PostgreSQL database server (via Toolbox) |
| `nuts org.apache.derby:derbynet` | Apache Derby network server |
| `nuts com.h2database:h2` | H2 database — starts web console at http://localhost:8082 |
| `nuts org.hsqldb:hsqldb` | HSQLDB database server |

### PostgreSQL — run a portable local instance

No `sudo`. No `/etc/postgresql`. No conflict with any system-installed PostgreSQL.

```bash
PGDATA=~/pgdata
nuts org.postgresql:postgresql-server initdb -D $PGDATA
echo "port = 8666" >> ~/pgdata/postgresql.conf
echo "unix_socket_directories = '/tmp'" >> $PGDATA/postgresql.conf
nuts org.postgresql:postgresql-server pg_ctl -D ~/pgdata -l ~/pgdata/logfile start
```

### Derby — start and stop the network server

```bash
# start
nuts org.apache.derby:derbynet start -p 1527

# shutdown
nuts org.apache.derby:derbynet shutdown -p 1527
```

### H2 — one-command database with web console

```bash
nuts com.h2database:h2
# web console available at http://localhost:8082
```

### HSQLDB — server mode

```bash
nuts org.hsqldb:hsqldb server --database.0 ~/hsqldb/mydb --dbname.0 mydb
```

---

## Developer Tools

| Command | Description |
|---|---|
| `nuts org.apache.maven:mvn` | Apache Maven (latest version) |
| `nuts nsh` | Portable bash-compatible shell with structured output |
| `nuts nops` | Developer-friendly DevOps command-line tool |
| `nuts org.jd:jd-gui` | Java decompiler GUI |
| `nuts com.puppycrawl.tools:checkstyle` | Checkstyle code style checker |
| `nuts org.springframework.boot:spring-boot-cli init --dependencies=web,data-jpa my-project` | Scaffold a Spring Boot project |
| `nuts noapi` | Generate professional PDF from OpenAPI 3.0 / Swagger spec |
| `nuts com.jgoodies:jdiskreport` | JDiskReport — analyze disk space usage |

### Maven — run in any project folder

```bash
nuts org.apache.maven:mvn --version
nuts org.apache.maven:mvn clean install
nuts org.apache.maven:mvn dependency:tree
```

### nsh — structured output from shell commands

nsh is a bash-compatible shell shipped with nuts, extended with structured output formats:

```bash
nuts nsh -c "ls --json"
nuts nsh -c "ls --table"
nuts nsh -c "ls --tree"
nuts nsh -c "ls --xml"
nuts nsh -c "ls --yaml"
nuts nsh -c "ls --tson"
```

### nsh — cross-protocol file operations

The nuts runtime exposes a unified path model across all protocols.
Every nuts-aware tool inherits it — not just nsh.

```bash
# copy from HTTP to a remote server over SSH
nuts nsh -c "cp http://files.example.com/app.jar ssh://user@prod-server/deploys/"

# tail a remote log file
nuts nsh -c "cat ssh://user@myserver/var/log/myapp.log | grep ERROR"

# copy between two remote servers without a local intermediate
nuts nsh -c "cp ssh://user@server1/data/dump.sql ssh://user@server2/restore/dump.sql"
```

Supported protocols: `http`, `https`, `ssh`, `htmlfs`, `dotfilefs`, local paths.

---

## Security

| Command | Description |
|---|---|
| `nuts com.google.tsunami:tsunami-main --ip-v4-target=127.0.0.1` | Google Tsunami network security scanner |

---

## Presentation & Documents

| Command | Description |
|---|---|
| `nuts ntexup show` | LaTeX-like presentation tool |
| `nuts noapi` | Generate professional PDF from OpenAPI 3.0 / Swagger spec |

---

## Graph & Visualization

| Command | Description |
|---|---|
| `nuts com.github.todense:omnigraph` | Omnigraph — graph visualization tool |

---

## Productivity & Personal

| Command | Description |
|---|---|
| `nuts org.jmeld:jmeld` | Folder diff tool |
| `nuts kifkif` | Find duplicate files |
| `nuts jpass:jpass` | JPass — password manager |
| `nuts pnote` | pnote — note taking tool |

---

## Games

| Command | Description |
|---|---|
| `nuts io.github.jiashunx:masker-flappybird` | Flappy Bird |
| `nuts com.github.anuken:mindustry-desktop` | Mindustry (desktop) |
| `nuts com.github.anuken:mindustry-server` | Mindustry (server) |

---

## Want more?

The commands above are just a curated sample.
Any application published on Maven Central can likely be run with nuts today — no repackaging, no fat-jars, no setup.

```bash
nuts search <keyword>                    # search available packages
nuts install <groupId>:<artifactId>      # install any Maven artifact
nuts <groupId>:<artifactId>              # install and run in one step
```

→ [Learn more about nuts](https://thevpc.github.io/nuts)