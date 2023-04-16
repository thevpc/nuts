
---
id: help-cmd
title: Help Command
sidebar_label: Help Command
---
${{include($"${resources}/header.md")}}


This command, as you should have guessed, show. help manual of your favorite command or of nuts it self.
```
me@linux:~> nuts help version
version :
nuts version
nuts --version
nuts -version
nuts -v
      show version and exit
...
```
will show help of the **version** command.

Usually, all nuts commands support the '--help' option command that should also show this very same help.
So the latter command is equivalent to
```
me@linux:~> nuts version --help
```
