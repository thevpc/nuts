---
title: Why Nuts Application Framework (NAF)
---


or, "Why should I consider implementing my terminal application using Nuts Application Framework (NAF)?"
First of all, NAF is a simple 300k jar so for what it provided to you, you would be surprised. 
Indeed, implementing your application using NAF will provide you a clean way to :

* seamless integration with **```nuts```** and all other NAF applications (obviously!)

* support standard file system layout (XDG) where config files and log files are not necessarily in the same folder see [Nuts File System](../04-nuts-concepts/04-filesystem) for more details.

* support application life cycle events (onInstall, onUninstall, onUpdate), 

* standard support of command line arguments

* dynamic dependency aware class loading

* terminal coloring, and terminal components (progress bar, etc...)

* json, yaml, xml, table, tree and plain format support out of the box as output to all your commands

* pipe manipulation when calling sub processes

* advanced io features (persistence Locks, monitored downloads, compression, file hashing....)

* standard ways to support and use installed platforms (installed JRE, JDK, ...)

* and lots more...
