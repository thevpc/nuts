---
id: settings-cmd
title: Settings
---

## Synopsis

```sh
nuts settings <subcommand> [options]
```

## Description

The `settings` command is the primary configuration tool for managing **nuts** workspaces. It acts as a meta-command with numerous subcommands to handle repositories, group imports, aliases, desktop integration (NDI), user security, JDK installations, logging, backups, and more.

By using the `settings` command, administrators can tailor the environment to their specific deployment needs, ensuring proper artifact resolution, tight security, and customized command aliases.

## Subcommands Overview

### Managing Repositories
Repositories define where **nuts** searches for and downloads artifacts.
* `settings add repo <url>`: Adds a new repository to the workspace.
* `settings remove repo <id>`: Removes an existing repository.
* `settings enable repo` / `settings disable repo`: Enables or disables a repository.
* `settings edit repo`: Edits repository configuration.
* `settings list repos`: Lists all configured repositories.

### Managing Imports
Group imports allow you to shorten artifact IDs by implicitly searching within designated group prefixes.
* `settings add import <groupId>`: Adds a group ID to the import list.
* `settings remove import <groupId>`: Removes a group ID.
* `settings list imports`: Lists all active group imports.

### Managing Aliases
Aliases allow you to create custom commands with preset arguments.
* `settings add alias <name> <command>`: Creates a new alias.
* `settings remove alias <name>`: Deletes an alias.
* `settings list aliases`: Displays all registered aliases.

### Desktop Integration (NDI)
Launchers and desktop integration integrate **nuts** artifacts with the host OS.
* `settings ndi [options] <package>`: Manages desktop integration (menu entries, desktop shortcuts, icons).

### Managing Java (JDKs)
**nuts** can provision and manage specific JDK versions required by artifacts.
* `settings add java --search`: Scans the system for available JDKs.
* `settings add java <folder>`: Registers a new JDK installation from a specific path.
* `settings remove java <name>`: Removes a registered JDK.
* `settings list java`: Lists registered JDKs.

### Managing Security & Users
The security model supports user authentication and restricted execution.
* `settings secure`: Enables security mode for the workspace.
* `settings unsecure`: Disables security mode.
* `settings add user <username>`: Adds a new authorized user.
* `settings remove user <username>`: Removes a user.
* `settings list users`: Lists all users.
* `settings password`: Change the current user's password.

### Maintenance & Operations
* `settings backup --file=<file>` / `settings restore --file=<file>`: Backs up or restores the workspace configuration.
* `settings connect --password=<password> user@host:port`: Connects to remote environments.
* `settings delete bin|conf|var|log|temp|cache|run`: Clears specific workspace directories.
* `settings update stats`: Updates workspace statistics.
* `settings get log level` / `settings set log level`: Manages the workspace logging level.

## Examples

Add a custom Maven repository:
```bash
nuts settings add repo https://my-company.com/maven/
```

Create a shortcut alias for a frequently used command:
```bash
nuts settings add alias nsh-ls "nsh -c ls"
```

Register a specific JDK version for older artifacts:
```bash
nuts settings add java /usr/lib/jvm/java-11-openjdk/
```

Create desktop shortcuts for an application:
```bash
nuts settings ndi --menu --desktop net.thevpc.app:netbeans-launcher
```

Backup the workspace configuration:
```bash
nuts settings backup --file=my-workspace-backup.zip
```
