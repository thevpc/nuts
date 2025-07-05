---
title: Spring Boot Integration
---
nuts can work flawlessly with spring boot applications. You just need one dependency and implement a NApplication interface.

Add the following dependency to you spring boot project
```xml
    <dependency>
        <groupId>net.thevpc.nuts</groupId>
        <artifactId>nuts-spring-boot</artifactId>
        <version>0.8.6.0</version>
    </dependency>
```

Add @NApp.Info `NApplication` in your `SpringBootApplication` top class.

```java
@NApp.Info
@SpringBootApplication
@Import(NutsSpringBootConfig.class)
public class AppExample {
    public static void main(String[] args) {
        SpringApplication.run(AppExample.class, args);
    }

    @NApp.Main
    public void run() {
        NOut.println("Hello ##World##");
    }
}
```

Now you can inject Nuts objects in your beans

```java
@Component
public class MyBean {
    @Autowired NSession session;
    @Autowired NWorkspace workspace;
    @Autowired NElements elems;
    @Autowired NIO nio;
    @Autowired NScheduler scheduler;
    @Autowired NTerminal term;
    @Autowired NPrintStream out;
}

```
