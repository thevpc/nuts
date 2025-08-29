---
title: Maven Dependency
---


to make use of **nuts** library  you need add the dependency  ```net.thevpc.nuts#nuts-lib:``` and provide a hint to maven to point to the right repository ``` https://maven.thevpc.net```

## Configure your pom.xml

```xml
    <dependencies>
        <dependency><groupId>net.thevpc.nuts</groupId><artifactId>nuts</artifactId><version>{{apiVersion}}</version></dependency>
    </dependencies>
    <repositories>
        <repository><id>thevpc</id><url>https://maven.thevpc.net</url></repository>
    </repositories>
    
```

