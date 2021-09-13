---
id: aliases
title: Aliases
sidebar_label: Aliases and Imports
---

Aliases, Imports and launchers, are three features in nuts where you can call artifacts with a simple word instead of using the full id. Indeed, usually, artifacts are uniquely identified by groupId, artifactId, version and classifier (whenever applicable). This is kind of cumbersome if most of the cases:

```bash
nuts net.thevpc.toolbox.nsh:nsh#0.8.3.0
```

## Imports
Imports help you discard groupId and call/install artifacts using only artifactId. You can as an example
import 'com.my-company' and as a result any artifact under 'com.mycompany' is resolved automatically.
Actually 'com.my-company:my-app' and 'com.my-company.my-app:my-app' are

```bash
nuts net.thevpc.toolbox.nsh:nsh#0.8.3.0
# becomes
nuts settings add import net.thevpc.toolbox
# now call it simply with
nuts nsh#0.8.3.0
# or even simpler with
nuts nsh
```
As a matter of fact, there are a couple of imports automatically defined 'net.thevpc' and 'net.thevpc.toolbox' 

## Aliases
Aliases help you define your own command by calling existing artifacts and defining some arguments as well. It is very similar to shell aliases where you define 'll' as and alias to '/bin/ls -l' for example.

```bash
nuts net.thevpc.toolbox.nsh:nsh#0.8.3.0
# becomes
nuts settings add alias nshl='net.thevpc.toolbox.nsh:nsh#0.8.3.0 --list'
# now call it simply with
nuts nshl
```

## Launchers
Launchers help you define your own system command by calling nuts with existing artifacts and even creating Desktop Environment Shortcuts and Icons (whenever GUI is applicable). 


```bash
nuts net.thevpc.toolbox.nsh:nsh#0.8.3.0
# becomes
nuts settings add launcher -- nshl='net.thevpc.toolbox.nsh:nsh#0.8.3.0 --list'
# now call it simply with
nuts nshl
```
