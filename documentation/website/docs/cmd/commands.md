---
id: cmds
title: Nuts Commands
sidebar_label: Nuts Commands
---


Nuts supports multiple types of commands (internal , external), multiple types of execution (embedded, spawn, and system execution) and multiple modes of execution (effective, dry).

Internal Commands include:
* welcome
* help
* version
* info
* license
* search
* fetch
* which
* exec
* install
* uninstall
* update
* reinstall
* check-updates
* deploy
* push
* bundle


### 1.1.7 search command

### 1.1.8 fetch command
**fetch** command is used to download content of an artifact when you exactly know of it's nuts long id (which is required). This will download a cached version of the artifact in the local machine (the artifact passes to 'fetched' status)
```
me@linux:~> nuts fetch net.vpc.app:netbeans-launcher#1.2.2
```
### update and check-updates commands
### deploy and push commands



