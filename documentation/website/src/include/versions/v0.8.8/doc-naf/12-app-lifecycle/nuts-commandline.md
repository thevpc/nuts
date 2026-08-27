---
title: Command Line Arguments
---


## Nuts Application Framework CommandLine

Application Command line can be retrieved via `NApp` instance:

```java
    NCmdLine c1= NApp.of().getCmdine();
```

## Exec / Autocomplete modes

```java
    NCmdLine c= NApp.of().getCmdine();
    if(c.isExecMode()){
        ///    
    }
```

