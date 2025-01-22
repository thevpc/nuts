---
title: Does nuts support only jar packaging
---

Not only. **```nuts```** supports all types of packaging, particularly, those supported by maven. 
This includes  pom , jar , maven-plugin , ejb , war , ear , rar.
**```nuts```** is also intended to support any "thing" including "exe" ,"dll", "so", "zip" files, etc.
**```nuts```** differs from maven as it defines other properties to the artifact descriptor (aka pom in maven) : os (operating system), arch (hardware architecture), osdist (relevant for linux for instance : opensuse, ubuntu) and platform (relevant to vm platforms like java vm, dotnet clr, etc).
Such properties are queried to download the most appropriate binaries for the the current environment.
