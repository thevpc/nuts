
## STEP 1


```sh
nuts --verbose -ZyS -r=dev,maven-central install ntomcat
```

## EXPECTATION

* NO_ERROR (No Exception StackTrace in the result)

## STEP 2


```sh
nuts -ZyS -r=dev,maven-central install ntomcat
```

## EXPECTATION

* NO_ERROR (No Exception StackTrace in the result)

```
     __        __       
  /\ \ \ _  __/ /______   Network Updatable Things Services
 /  \/ / / / / __/ ___/   The Free and Open Source Package Manager
/ /\  / /_/ / /_(__  )    for Java and other Things ... by thevpc
\_\ \/\__,_/\__/____/     http://github.com/thevpc/nuts
    version 0.8.3.0-rc1
location:/home/vpc/.config/nuts/default-workspace  (Wheat default-workspace)
╭───────────────────────────────────────────────────────────────────────╮
│ This is the very first time nuts has been launched for this workspace │
╰───────────────────────────────────────────────────────────────────────╯

looking for java installations in default locations...
detected java jdk 15.0.5 at /usr/lib64/jvm/jre-15
detected java jdk 11.0.13 at /usr/lib64/jvm/jre
detected java jdk 11.0.13 at /usr/lib64/jvm/java-11-openjdk-11
detected java jdk 11.0.13 at /usr/lib64/jvm/jre-11
detected java jdk 15.0.5 at /usr/lib64/jvm/java-15-openjdk
detected java jdk 15.0.5 at /usr/lib64/jvm/java-15-openjdk-15
detected java jdk 15.0.5 at /usr/lib64/jvm/jre-15-openjdk
detected java jdk 15.0.5 at /usr/lib64/jvm/java-15
detected java jdk 11.0.13 at /usr/lib64/jvm/java
detected java jdk 1.8.0_312 at /usr/lib64/jvm/java-1.8.0-openjdk
detected java jdk 1.8.0_312 at /usr/lib64/jvm/java-1.8.0
detected java jdk 11.0.13 at /usr/lib64/jvm/jre-openjdk
detected java jre 1.8.0_312 at /usr/lib64/jvm/jre-1.8.0-openjdk
detected java jre 1.8.0_312 at /usr/lib64/jvm/jre-1.8.0
detected java jdk 11.0.13 at /usr/lib64/jvm/java-11-openjdk
detected java jdk 11.0.13 at /usr/lib64/jvm/java-openjdk
detected java jdk 11.0.13 at /usr/lib64/jvm/jre-11-openjdk
detected java jdk 1.8.0_312 at /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0
detected java jdk 11.0.13 at /usr/lib64/jvm/java-11
install java jre (JDK) 1.8.0_202 at /data/programs/Development/SDK/Java/jdk1.8.0_202/jre
install java jdk (OpenJDK) 1.8.0_312 at /usr/lib64/jvm/java-1.8.0
install java jre (OpenJDK) 1.8.0_312 at /usr/lib64/jvm/jre-1.8.0
install java jdk (OpenJDK) 11.0.13 at /usr/lib64/jvm/java
install java jdk (OpenJDK) 15.0.5 at /usr/lib64/jvm/java-15
5 new java installation locations added...
you can always add another installation manually using 'nuts settings add java' command.
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-env.sh
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-env.fish
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-init.sh
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-init.fish
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/bin/nuts
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-term-init.sh
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/inc/.nuts-term-init.fish
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/bin/nuts-term
looking for recommended companion tools to install... detected : net.thevpc.nuts.toolbox:nsh
install net.thevpc.nuts.toolbox:nsh#0.8.3.0-rc1 ...
require net.thevpc.nuts.lib:nlib-ssh#0.8.3.0-rc1 from remote repository (dev).
require com.jcraft:jsch#0.1.55 from remote repository (dev).
install command date
install command whoami
install command ls
install command wget
install command ssh
install command echo
install command source
install command type
install command unzip
install command dirname
install command head
install command xml
install command cat
install command chmod
install command json
install command mkdir
install command zip
install command uname
install command test
install command autocomplete
install command grep
install command tail
install command builtin
install command false
install command history
install command env
install command jps
install command cp
install command command
install command props
install command basename
install command true
install command rm
install command pwd
re-registered 34 nsh commands : autocomplete, basename, builtin, cat, chmod, command, cp, date, dirname, echo, env, false, grep, head, history, jps, json, ls, mkdir, props, pwd, rm, source, ssh, tail, test, true, type, uname, unzip, wget, whoami, xml, zip 
update file /home/vpc/.bashrc
force updating .nuts-env.sh, .nuts-env.fish, .nuts-init.sh, .nuts-init.fish, nuts, .nuts-term-init.sh, .nuts-term-init.fish, nuts-term, .bashrc, .profile, .zshenv, .cshrc, .kshrc, config.fish, net.thevpc.nuts-nuts.desktop, net.thevpc.nuts-nuts.menu to point to workspace /home/vpc/.config/nuts/default-workspace
create file /home/vpc/.config/nuts/default-workspace/apps/id/net/thevpc/nuts/nuts/0.8.3-rc1/bin/nsh
install net.thevpc.nuts.toolbox:nsh#0.8.3.0-rc1 from remote repository (dev). set as default.
install net.thevpc.nuts.toolbox:ntomcat#0.8.3.0-rc1 ...
require org.jsoup:jsoup#1.12.1 from remote repository (maven-central).
install net.thevpc.nuts.toolbox:ntomcat#0.8.3.0-rc1 from remote repository (dev). set as default.

```

## STEP 3

```sh
nuts install ntomcat
```

## EXPECTATIONS

the following installed artifact is going to be re-reinstalled : ntomcat#0.8.3.0-rc1?repo=dev
should we proceed?
(default is y, accepts y, n) ? : y
re-install net.thevpc.nuts.toolbox:ntomcat#0.8.3.0-rc1 ...
re-install net.thevpc.nuts.toolbox:ntomcat#0.8.3.0-rc1 from remote repository (dev).

## STEP 4

```sh
nuts ntomcat start
```

## EXPECTATIONS

```
the following new artifact is going to be installed : org.apache.catalina:apache-tomcat#10.0.0-M1?repo=apache-tomcat
should we proceed?
(default is y, accepts y, n) ? :
▁▃▆     copy           https://archive.apache.org/dist/tomcat/tomcat-10/v10.0.0-M1/bin/apache-tomcat-10.0.0-M1.zip to /home/vpc/.config/nuts/default-workspace/temp/id/net/thevpc/nuts/toolbox/ntomcat/0.8.3.0-rc1/apache-tomcat-4834836735971916656/cache/org/apache/catal   █▆▃     copy           https://archive.apache.org/dist/tomcat/tomcat-10/v10.0.0-M1/bin/apache-tomcat-10.0.0-M1.zip to /home/vpc/.config/nuts/default-workspace/temp/id/net/thevpc/nuts/toolbox/ntomcat/0.8.3.0-rc1/apache-tomcat-4834836735971916656/cache/org/apache/catalinstall org.apache.catalina:apache-tomcat#10.0.0-M1 ...
[default] Tomcat installed to catalina home null
install org.apache.catalina:apache-tomcat#10.0.0-M1 from remote repository (apache-tomcat). set as default.
█▆▃    <main>         lib      search folder ~/.config/.../apache-tomcat/10.0.0-M1                                                 [default] updated catalina base /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default
[default] updated catalina base /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default
[default] starting Tomcat on port 8080. CMD=JAVA_HOME=/data/programs/Development/SDK/Java/jdk1.8.0_202/jre JRE_HOME=/data/programs/Development/SDK/Java/jdk1.8.0_202/jre JAVA_OPTS=-Dnuts-config-name=default CATALINA_HOME=/home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 CATALINA_BASE=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default CATALINA_OUT=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/logs/catalina.out CATALINA_TMPDIR=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/catalina.sh start.
Using CATALINA_BASE:   /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default
Using CATALINA_HOME:   /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1
Using CATALINA_TMPDIR: /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp
Using JRE_HOME:        /data/programs/Development/SDK/Java/jdk1.8.0_202/jre
Using CLASSPATH:       /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/bootstrap.jar:/home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/tomcat-juli.jar
Tomcat started.
[default] Tomcat started on port 8080.
```

## STEP 5

```sh
nuts ntomcat status
```


## EXPECTATIONS

```
[default] Tomcat running.

```


## STEP 5

```sh
nuts ntomcat stop
```

## EXPECTATIONS

```
[default] updated catalina base /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default
[default] stopping Tomcat. CMD=JAVA_HOME=/data/programs/Development/SDK/Java/jdk1.8.0_202/jre JRE_HOME=/data/programs/Development/SDK/Java/jdk1.8.0_202/jre JAVA_OPTS=-Dnuts-config-name=default CATALINA_HOME=/home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 CATALINA_BASE=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default CATALINA_OUT=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/logs/catalina.out CATALINA_TMPDIR=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/catalina.sh stop.
Using CATALINA_BASE:   /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default
Using CATALINA_HOME:   /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1
Using CATALINA_TMPDIR: /home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp
Using JRE_HOME:        /data/programs/Development/SDK/Java/jdk1.8.0_202/jre
Using CLASSPATH:       /home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/bootstrap.jar:/home/vpc/.config/nuts/default-workspace/apps/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1/bin/tomcat-juli.jar
[default] Tomcat stopped.

```


## STEP 5

```sh
nuts ntomcat status
```

## EXPECTATIONS

```
[default] Tomcat stopped.
```


