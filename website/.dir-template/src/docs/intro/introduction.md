---
id: introduction
title: Introduction
sidebar_label: Introduction
---

${include($"${resources}/header.md")}

## Introduction

**```nuts```** stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
artifacts, installing these  artifacts to the current machine and executing such  artifacts on need.
Each managed package  is also called a **```nuts```** which  is a **Network Updatable Thing Service** .
**```nuts```** artifacts are  stored  into repositories. A  **repository**  may be local for  storing local **```nuts```**
or remote for accessing  remote artifacts (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote artifacts are fetched and cached locally to save network
resources.

One manages a set of repositories called a  workspace. Managed **```nuts```**  (artifacts)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  **```nuts```**  (tool) to
resolve and download on-need dependencies over the wire.

**```nuts```** is a swiss army knife tool as it acts like (and supports) **maven** build tool to have an abstract
view of the the  artifacts dependency and like  **npm**, **pip** or **zypper/apt-get**  package manager tools  
to  install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed.

## Common Verbs

+ ```exec```               : execute an artifact or a command
+ ```install```, ```uninstall``` : install/uninstall an artifact (using its fetched/deployed installer)
+ ```deploy```, ```undeploy```   : manage artifacts (artifact installers) on the local repositories
+ ```update```             : update an artifact (using its fetched/deployed installer)
+ ```fetch```, ```push```        : download from, upload to remote repositories
+ ```search```             : search for existing/installable artifacts
+ ```welcome```            : a command that does nothing but bootstrapping **```nuts```** and showing a welcome message.
