---
title: Exec/Ps: Working with processes
---


**nuts** Library simplify creating and manipulating processes


## To create a new process

```java
    NSession session=Nuts.openWorkspace("-Z","-S");
int code=NExecCmd.of("ls", "-l").system().getResult();
String out=NExecCmd.of("nsh", "ls","--table")
        .getOutputString();
```


## Find processes
    NPs ps=NPs.of();
    List<NPsInfo> processes=pf.getResultList().toList();
## Kill process

```java
NPs ps=NPs.of();
    if(ps.isSupportedKillProcess()){
        ps.killProcess("1234");
    }
```


