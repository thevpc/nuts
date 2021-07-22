---
id: running
title: Running Nuts
sidebar_label: Running Nuts
---

${include($"${resources}/header.md")}

## Running a deployed artifact
You can run any jar using **```nuts```** as far as the jar is accessible from one of the supported repositories.
By default, **```nuts```** supports:
 + maven central
 + local maven folder (~/.m2)

You can configure other repositories or even implement your own if you need to.

The jar will be parsed to check form maven descriptor so that dependencies will be resolved and downloaded on the fly.
Then, all executable classes (public with static void main method) are enumerated. You can actually run any of them when prompted. Any jar built using maven should be well described and can be run using its artifact long id.

## Artifact Long Ids
**```nuts```** long ids are a string representation of a unique identifier of the artifact. It has the following form :

```java
groupId:artifactId#version
```

for instance, to install ```netbeans-launcher``` (which is a simple UI helping launch of multiple instances of netbeans), you can issue

```bash
  nuts net.vpc.app:netbeans-launcher#1.2.2
```

You do agree that this can be of some cumbersome to type. So you can simplify it to :

```bash
  nuts netbeans-launcher
```

In this form, **```nuts```** will auto-detect both the ```groupId``` and the ```version```. The group id is detected if it is already imported (we will see later import a groupId). 
By default, there is a couple of groupIds that are automatically imported :

  + ```net.vpc.app``` (contains various applications of the author)
  + ```net.vpc.nuts.toolbox``` (contains various companion tools of **```nuts```**, such as ```nsh```, ```nadmin```, ...)

And it turns out, hopefully, that netbeans-launcher belongs to an imported groupId, so we can omit it.
Besides, if no version is provided, **```nuts```** will also auto-detect the best version to execute. If the application is already installed, the version you choose to install will be resolved. If you have not installed any, the most recent version, obviously, will be detected for you.

## Artifact Installation
Any java application can run using **```nuts```** but it has to be installed first. If you try to run the application before installing it, you will be prompted to confirm installation.
To install our favorite application here we could have issued :
```bash
  nuts install netbeans-launcher
```
But as we have tried to run the application first, it has been installed for us (after confirmation).

## Multiple Artifact version Installation
One of the key features of **```nuts```** is the ability to install multiple versions of the same application.
We can for instance type :
```bash
  nuts install netbeans-launcher#1.2.2
  # then
  nuts install netbeans-launcher#1.2.0
```
Now we have two versions installed, the last one always is considered default one.
you can run either, using it's version
```bash
  nuts netbeans-launcher#1.2.2 &
  # or
  nuts netbeans-launcher#1.2.0 &
```
Actually, when you have many versions installed for the same artifact and you try to run it without specifying the version, the last one installed will be considered. To be more precise, an artifact has a default version when it is installed. This default version is considered when no explicit version is typed.
In our example, when we type 
```
  nuts netbeans-launcher &
```
the 1.2.0 version will be invoked because the artifact is already installed and the default version points to the last one installed. So if you want to switch back to version 1.2.2 you just have to re-install it. Don't worry, no file will be downloaded again, nuts will detect that the version is not marked as default and will switch it to.

## Searching artifacts
Now let's take a look at installed artifacts. We will type :
```bash
  nuts search --installed
```
This will list all installed artifacts. We can get a better listing using long format :
```bash
  nuts search --installed -l
```
you will see something like

```
I-X 2019-08-21 04:54:22.951 anonymous vpc-public-maven net.vpc.app:netbeans-launcher#1.2.0
i-X 2019-08-21 04:54:05.196 anonymous vpc-public-maven net.vpc.app:netbeans-launcher#1.2.2
```

The first column here is a the artifact status that helps getting zipped information of the artifact. the 'I' stands for 'installed and default' whereas, 'i' is simply 'installed'. The 'X' stands for 'executable application', where 'x' is simply 'executable'. Roughly said, executable applications are executables aware of (or depends on) **nuts**, as they provide a special api that helps nuts to get more information and more features for the application. As an example, executable applications have special OnInstall and OnUninstall hooks called by nuts.
The second and the third columns are date and time of installation. The fourth column points to the installation user. When Secure mode has not been enabled (which is the default), you are running nuts as 'anonymous'.
The fifth column shows the repository from which the package was installed. And the last column depicts the artifact long id.

## Running local jar file with its dependencies
Let's suppose that my-app.jar is a maven created jar (contains META-INF/maven files) with a number of dependencies. **```nuts```** is able to download on the fly needed dependencies, detect the Main class (no need for MANIFEST.MF) and run the 
application. If a Main-Class Attribute was detected in a valid MANIFEST.MF, il will be considered.
If more than one class is detected with a main method, **```nuts```** will ask for the current class to run.

When you run a local file, **```nuts```** will behave as if the app is installed (in the given path, an no need to invoke install command). Local files are detected if they are denoted by a valid path (containing '/' or '\' depending on the underlying operating system).
Dependencies will be downloaded as well (and cached in the workspace)

```bash
nuts ./my-app.jar some-argument-of-my-app
```
If you need to pass JVM arguments you have to prefix them with "--exec". So if you want to fix maximum heap size use 

```bash
nuts --exe -Xms1G -Xmx2G ./my-app.jar argument-1 argument-2
```
