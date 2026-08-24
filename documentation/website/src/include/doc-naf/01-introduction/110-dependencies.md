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

:::info
A note on `Nuts.require()` and Workspaces

`Nuts.require()` in the example above is a convenience for demos and single-app mains. It creates an in-memory singleton workspace bound to the current JVM.

In production, Nuts never relies on a global singleton. A workspace is a filesystem-isolated environment (config, apps, cache, log...) selected via `--workspace=path` or programmatically:
```java
NWorkspace wsA = Nuts.openWorkspace("-w=/opt/ws-a"); 
NWorkspace wsB = Nuts.openWorkspace("-w=/opt/ws-b"); 
wsA.runWith(() -> { // everything inside uses wsA: NOut, NPath, repos, etc. NOut.println("running in A"); });
```
Yes, you can create and switch between multiple NWorkspace instances in the same JVM. Isolation is by filesystem root, not by classloader, so it works reliably for multi-tenant services and tests.
:::
