---
id: exec-cmd
title: Exec and Which Commands
sidebar_label: Exec and Which Commands
---

${{include($"${resources}/header.md")}}

**exec** command runs another command and **which** command does a dry run of it.

When one types
```
nuts netbeans-command
```
it is actually equivalent to
```
nuts exec netbeans-command
```
What is helpful with **exec** is that it permits passing extra parameters to application executors. In **nuts**, an application executor is an artifact that can be used to run other artifacts. For instance **nsh**, which is a **nuts** companion, is an executor for all "*.nsh" artifacts (yest script files are artifacts too). Some executors are specially handled such as "java" executor that is used to run all jars and basically all java based artifacts. Java executor for instance supports all java standard vm option arguments
```
me@linux:~> nuts exec -Xmx1G netbeans-launcher
```
Here we pass -Xmx1G option argument to java executor because __netbeans-launcher__ will be resolved as a java based artifact.
For what concerns **which** command, it does not really execute the command, it just resolves the command execution
```
me@linux:~> nuts which version ls
version : internal command 
ls : nuts alias (owner maven-local://net.thevpc.app.nuts.toolbox:nsh#${{latestRuntimeVersion}} ) : maven-local://net.thevpc.app.nuts.toolbox:nsh#${{latestRuntimeVersion}} -c ls
```
Here **which** returns that **version** is an internal command while **ls** is an alias to an artifact based command (nsh -c ls) which is called a "nuts alias". As you can see, ls is actually a sub command of nsh artifact.

#### 1.2 External Commands
External commands are commands that will invoke another artifact. for instance
```
nuts netbeans-command
```
is running an external command which is net.thevpc.app:netbeans-launcher#1.2.2 artifact.

#### 1.2 External Files & URLs
You can run any jar file using **nuts** as far as it fulfills two points : the files must contain a supported descriptor (if it is compiled with maven, it already has the supported descriptor) and the file should be typed as a path (it must contain a '/' or '\' separator)

```
wget -N https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar
nuts ./netbeans-launcher-1.2.2.jar
```
You can even run a remove file using its url format :

```
nuts https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar
```

## 2. Execution types
### 2.1 spawn
This is the default execution type where any external command will spawn a new process to execute within.
Internal commands are not affected by this mode and are executed, always, in the current vm process (with embedded type).
```
me@linux:~> nuts --spawn ls
```
### 2.2 embedded
In this type the command will try not to spawn a new process but load in the current vm the commmand to run (as far as it is a java command)
```
me@linux:~> nuts --embedded ls
```
### 2.3 syscall
In this type, the command execution is delegated to the underlying operating system end hence will also swan a new process.
```
me@linux:~> nuts --syscall ls
```

## 3 Execution modes
### 3.1 effective execution
This is the default execution mode where the command is really and effectively ran.
### 3.2 dry execution
In this mode, the command will be ran in dry mode with no side effects which implies a "simulation" of the execution.
```
me@linux:~> nuts --dry version
[dry] internal version 
```


