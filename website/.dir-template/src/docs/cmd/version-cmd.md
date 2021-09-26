---
id: version-cmd
title: Version Command
sidebar_label: Version Command
---
${{include($"${resources}/header.md")}}

This command will show **nuts** version. It is helpful to note that **nuts** has a couple of components : api and impl.
api is the **nuts** bootstrap jar (actually nuts-*.jar, <300Ko of size) that contains only the minimum code to use nuts and to download the full implementation (1.5Mo of size) : impl component. Usually, the implementation version starts with the api version but this should be no rule.
```
me@linux:~> nuts version
0.5.7/0.5.7.0
```
Here the **version** command show api version (0.5.7) and the impl version (0.5.7.0)
