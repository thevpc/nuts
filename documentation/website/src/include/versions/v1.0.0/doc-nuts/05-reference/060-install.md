---
id: install-cmd
title: Install
---

## Synopsis

```sh
nuts install [options] <artifact-id>
```

## Description

To be executed securely and optimally, an artifact should generally be installed. While you can run URLs and local paths directly, installing an artifact ensures it is integrated properly into your workspace.

Installation can be triggered automatically upon first execution (where you are prompted), or manually using the `install` command.

```bash
nuts install net.thevpc.app:netbeans-launcher#1.2.2
```

You can use wildcard artifact queries to install matching components:
```bash
nuts install "net.thevpc.app:netbeans-*"
```

If an artifact is already installed and you need to reinstall or repair it, use the `--force` flag (or alternatively, the `reinstall` command):
```bash
nuts install --force net.thevpc.app:netbeans-launcher#1.2.2
```

### Switching Versions

When multiple versions of an artifact are installed, they remain isolated and execute side-by-side. The most recently installed version automatically becomes the default when no version is specified during execution. 

To change the default version, simply run `install` on the desired existing version without the `--force` flag.

```bash
$ nuts install net.thevpc.app:netbeans-launcher#1.2.2
$ nuts netbeans-launcher
1.2.2
$ nuts install net.thevpc.app:netbeans-launcher#1.2.1
$ nuts netbeans-launcher
1.2.1
```

You can find all installed artifacts using `nuts search --installed`.

## Installation Strategies

The installation system handles dependencies seamlessly using multiple strategies. The status of a package can be `installed` (explicitly requested by the user) or `required` (installed implicitly as a dependency).

* `require`: Installs the package and its dependencies as "required". Required packages are automatically uninstalled if no other installed package depends on them.
* `install`: Installs the package as a primary, first-class installed package.
* `reinstall`: Re-installs or re-requires the package and its dependencies.
* `repair`: Repairs a given dependency state.

### State Transition Matrix

| Status/Strategy | REQUIRE | INSTALL | REINSTALL | REPAIR |
|---|---|---|---|---|
| NOT_INSTALLED | REQUIRED | INSTALLED | INSTALLED? | ERROR |
| INSTALLED | INSTALLED REQUIRED | INSTALLED? | INSTALLED | INSTALLED |
| INSTALLED REQUIRED | INSTALLED REQUIRED | INSTALLED REQUIRED | INSTALLED REQUIRED | INSTALLED REQUIRED |
| REQUIRED | REQUIRED | INSTALLED REQUIRED | REQUIRED | REQUIRED |
| INSTALLED OBSOLETE | INSTALLED REQUIRED OBSOLETE | INSTALLED | INSTALLED | INSTALLED |
| INSTALLED REQUIRED OBSOLETE | INSTALLED REQUIRED | INSTALLED REQUIRED | INSTALLED REQUIRED | INSTALLED REQUIRED |
| REQUIRED OBSOLETE | REQUIRED OBSOLETE | INSTALLED REQUIRED | REQUIRED | REQUIRED |
