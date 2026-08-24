---
title: Command Line Arguments
---


## Nuts Application Framework CommandLine

Application Command line can be retrieved via `NApp` instance:

```java
    NCmdLine c1= NApplication.of().cmdLine();
```

## Exec / Autocomplete modes

```java
    NCmdLine c= NApplication.of().cmdLine();
    if(c.isExecMode()){
        ///    
    }
```

