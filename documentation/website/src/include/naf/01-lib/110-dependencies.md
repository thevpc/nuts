---
title: Maven Dependency
---


to make use of **nuts** library  you need add the dependency  ```net.thevpc.nuts#nuts-lib:0.8.6.0``` and provide a hint to maven to point to the right repository ``` https://maven.thevpc.net```

## Configure your pom.xml

```xml
    <dependencies>
        <dependency><groupId>net.thevpc.nuts</groupId><artifactId>nuts-lib</artifactId><version>0.8.6.0</version></dependency>
    </dependencies>
    <repositories>
        <repository><id>thevpc</id><url>https://maven.thevpc.net</url></repository>
    </repositories>
    
```

