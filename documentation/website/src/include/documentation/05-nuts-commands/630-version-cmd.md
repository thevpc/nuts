---
id: version-cmd
title: Version
---


This command will show **nuts** version. It's helpful to note that **nuts** has a couple of components : api and impl.
api is the **nuts** bootstrap jar (actually nuts-*.jar, ~500Ko of size) that contains only the minimum code to use nuts and to download the full implementation (3Mo of size) : impl component. Usually, the implementation version starts with the api version but this should be no rule.
```
me@linux:~> nuts version
0.8.5/0.8.5.0
```
Here the **version** command show api version (0.8.5) and the impl version (0.8.5.0)
