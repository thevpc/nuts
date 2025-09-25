---
title: Hello World
---


To make use of `NAF`  you need add the dependency  ```net.thevpc.nuts#nuts:{{apiVersion}}``` and provide a hint to maven to point to the right repository ``` https://maven.thevpc.net```

## Configure your pom.xml

```xml
    <dependencies>
        <dependency><groupId>net.thevpc.nuts</groupId><artifactId>nuts</artifactId><version>{{apiVersion}}</version></dependency>
    </dependencies>
    <repositories>
        <repository><id>thevpc</id><url>https://maven.thevpc.net</url></repository>
    </repositories>
    
```

## Bootstrap your Workspace

```java
    import net.thevpc.nuts.*;
    public class HelloWorld {
        public static void main(String[] args){
            Nuts.require();
        }
    }
```

## Use NAF components, anywhere in your app

```java
    import net.thevpc.nuts.*;
    public class HelloWorld {
        public static void main(String[] args){
            Nuts.require(); // <-- this command should be called only once per app
            NOut.println(NMsg.ofC("Hello %s","World"));
            runMethod();
        }
        public static void runMethod(){
            NOut.println(NMsg.ofV("Hello $v",NMaps.of("v","World")));
        }
    }
```

