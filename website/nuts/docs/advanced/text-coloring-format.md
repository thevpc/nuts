---
id: doc1
title: Style Guide
sidebar_label: Style Guide
---

```
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )
\_\ \/\__,_/\__/____/    version v0.7.0
```

# Nuts Text Coloring Format
**```nuts```** comes up with a simple coloring syntax that helps writing better looking portable command line programs.
standard output is automatically configured to accept the "Nuts Text Coloring Format" (NTCF) syntax. 
Though it remains possible to disable this ability using the --no-color standard option (or programmatically, 
see **```nuts```** API documentation). NTCF will be translated to the underlying terminal implementation using ANSI 
escape code on linux/windows terminals if available.

Here after a showcase of available NTCF syntax.

![text-coloring-format](text-coloring-format.png)