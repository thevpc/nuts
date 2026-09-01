---
id: help-cmd
title: Help Command
sidebar_label: Help Command
---

## Synopsis

```sh
nuts help [<command>]
```

## Description

The `help` command displays the manual and usage instructions for **nuts** itself or for specific commands.

To view the help manual for a specific command, pass the command name as an argument:

```bash
$ nuts help version

version :
nuts version
nuts --version
nuts -version
nuts -v
      show version and exit
...
```

Alternatively, all **nuts** commands inherently support the `--help` flag, which produces the exact same output. The following command is entirely equivalent:

```bash
$ nuts version --help
```

You can use `nuts help` without arguments to view the general system manual and a list of available commands.
