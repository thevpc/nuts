---
title: NAF Spring Boot Integration
---

nuts can work flawlessly with spring boot applications. You just need one annotation @NAppDefinition to mark your SpringBootApplication.

Add the following dependency to you spring boot project
```xml
    <dependency>
        <groupId>net.thevpc.nuts</groupId>
        <artifactId>nuts-spring-boot</artifactId>
        <version>{{runtimeVersion}}</version>
    </dependency>
```

Add @NAppDefinition in your `SpringBootApplication` top class.

```java
@NAppDefinition
@SpringBootApplication
@Import(NutsSpringBootConfig.class)
public class AppExample {
    public static void main(String[] args) {
        SpringApplication.run(AppExample.class, args);
    }

    @NAppRunner // not mandatory
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
    @Autowired NConcurrent concurrent;
    @Autowired NTerminal term;
    @Autowired NPrintStream out;
}

```
