---
id: cmds
title: Nuts Commands
sidebar_label: Nuts Commands
---

${{include($"${resources}/header.md")}}

Nuts supports multiple types of commands (internal , external), multiple types of execution (embedded, spawn, and system execution) and multiple modes of execution (effective, dry).

Internal Commands include:
* `welcome` : this is the default command that simply shows a welcome message and exits
* `help` : documentation and help command for `nuts` and applications and exits 
* `version` : show nuts version and exits
* `info` : show detailed `nuts` installation information and exits
* `license` : simply show `nuts` license and exits
* `search` : search for applications installed/to install
* `fetch` : download applications (without installing)
* `which` : resolve the appropriate application (or an internal command) for a given command
* `exec` : execute c(or an internal command)
* `install` : installs an application 
* `uninstall` : uninstalls an application
* `update` : updates an installed application
* `reinstall` : reinstall an existing application
* `check-updates` : checks for updates of an application (do not perform the update)
* `deploy` : deploys a local application (and its descriptors) to a local repository
* `push` : syncronizes the local repositry to it's remote peer and hence, deploy to a remote repository.
* `bundle` : creates a standalone bundle as a complete nuts workspace that serve a single application
* `settings` : this is main settings/configuration commands to manage nuts workspaces, apps, security, etc.


