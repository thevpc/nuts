---
id: introduction
title: Introduction
sidebar_label: Introduction
order: 1
---


**```nuts```** stands for **Network Updatable Things Services** tool and is a portable package manager for java (mainly) that handles remote artifacts, installs these artifacts to the current machine and executes such artifacts on need.
**```nuts```** solves the **fatjar** problem delegating the dependency resolution to the time when the application is to be executed and
simplifies the packaging process while being transparent to the build process. Actually, nuts uses **maven** **pom** descriptors to resolve
dependencies when the artifact is installed on the target machine, and it can use also other types of descriptors for other types of packages.

**```nuts```** artifacts are  stored  into repositories. A  **repository**  may be local for  storing local artifacts or remote for accessing remote artifacts (good examples  are  remote maven  repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network resources.

One manages a set of repositories called a  workspace (like **virtualenv** in **```pip```**). Managed **```nuts```**  (artifacts)  have descriptors that depicts dependencies between them. This dependency is seamlessly handled by  **```nuts```**  (tool) to resolve and download on-need dependencies over the wire.

**```nuts```** is a swiss army knife tool as it acts like (and supports) **maven** build tool to have an abstract view of the artifacts
dependency and like  **npm** and **pip** language package managers to  install and uninstall artifacts allowing multiple versions of the very
same artifact to  be installed. **```nuts```** is not exclusive for Java/Scala/Kotlin and other Java Platform Languages, by design it supports
multiple artifact formats other than jars and wars and is able to select the appropriate artifacts and dependencies according to the current OS, architecture and even Desktop Environment.



**```nuts```** common verbs are:

+ ```exec```               : execute an artifact or a command
+ ```which```              : detect the proper artifact or system command to execute
+ ```install```, ```uninstall``` : install/uninstall an artifact (using its fetched/deployed installer)
+ ```update```,```check-updates```  : search for updates
+ ```deploy```, ```undeploy```   : manage artifacts (artifact installers) on the local repositories
+ ```fetch```, ```push```        : download from, upload to remote repositories
+ ```search```             : search for existing/installable artifacts
+ ```welcome```            : a command that does nothing but bootstrapping **```nuts```** and showing a welcome message.
