---
id: automation
title: Automation
sidebar_label: Automation
---

```nuts``` has been design and implemented with automation and devops philosophy in mind.
```nuts``` Application Framework infrastructure provides a seamless support process automation
with structured output, including json, xml, yaml, tson and so on. You can for instance call the 
POSIX posix ls command and get the file list as ```json```. You can then process this ```json```
and extract meaningful information and pass it to the nex command using standard pipe mechanism.

Think of this as a general pattern for any and all commands you can run via nuts. Besides, 
automation includes dynamic classloading of on-the-fly dependencies (remotely resolved and downloaded)
to make usage of a feature you need such as installing a tomcat version that is compatible with the jre version you run.

Automation requires also partitioning, isolation, sand-boxing, security reinforcements and portability. this is ensured by workspace feature that helps isolating the application dependencies from other applications, authentication and authorisation mechanisms to limit access to nuts configurations (and hence available repositories used for dependency resolution) and to system resources (running with or without elevated privileges) and finally environment adaptability to handle appropriate support for each architecture (x86_32,itanium_64,...), operating system (linux, windows,...), shell (bash, zsh,...), platform (java, dotnet, ...) and desktop environment