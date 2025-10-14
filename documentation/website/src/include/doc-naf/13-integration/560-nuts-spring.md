---
title: NAF Spring Boot Integration
---

nuts can work flawlessly with spring boot applications. You just need one dependency and one annotation @NAppDefinition to mark your SpringBootApplication.

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

### Using Spring-Managed Beans in Nuts

Nuts can automatically integrate with Spring's application context. By default, `NBeanContainer` is wired with the Spring context, which allows any object managed by Spring to be referenced in Nuts using `NBeanRef`.

> **Important:** Only the `NBeanRef` itself is serialized or persisted. At runtime, the actual bean is resolved dynamically from the current bean container (e.g., Spring context). This means you can safely serialize or store Nuts objects without worrying about serializing the full bean state.

#### Example: Referencing Spring Beans

```java
// Assume jdbcStore is an instance of a persistent store
NRetryCallFactory factory = NConcurrent.of()
        .retryCallFactory()
        .withStore(jdbcStore); // optional persistence

// Register Spring beans by reference using NBeanRef
factory.of("something", NBeanRef.of("callSomeThingBean").as(NCallable.class))
       .setHandler(NBeanRef.of("resultSomeThingBean").as(NRetryCall.Handler.class))
       .setMaxRetries(5)
       .setRetryPeriod(NConcurrent.of().retryMultipliedPeriod(NDuration.ofSeconds(1), 1))
       .callAsync();

// Example: Custom handler implementation
public class ResultSomeThingHandler implements NRetryCall.Handler {
    @Override
    public void handle(NRetryCall.Result result) {
        if (result.isSuccess()) {
            logger.info("Retry call succeeded: {}", result.getValue());
        } else {
            logger.error("Retry call failed after {} attempts", result.getAttempts(), result.getException());
        }
    }
}
```

#### Bean Resolution and Error Handling

The `NBeanRef.of("beanName").as(ClassType.class)` call returns a proxy to the specified interface. No validation occurs at this point—the reference is simply stored. **Bean resolution happens at runtime when you invoke methods on the proxy.** If the referenced bean doesn't exist in the container when a method is called, a `NEmptyOptionalException` will be thrown at that moment, not when creating the reference.

```java
// This creates a reference but doesn't fail
NBeanRef.of("nonExistentBean").as(SomeInterface.class);

// This will throw an exception when resolve the bean and invoke the method
NBeanRef.of("nonExistentBean").as(SomeInterface.class).someMethod(); // ← Error thrown here
```

#### Notes

* `NBeanRef.of("beanName").as(ClassType.class)` works for any Spring-managed bean, not just retry calls.
* Bean references are resolved lazily at method invocation time, allowing for flexible deployment scenarios where beans may be reconfigured or reloaded.
* Persistent stores (like `jdbcStore`) are optional and provide state recovery when needed.
* This approach demonstrates how Nuts and Spring can work together, enabling robust integration for retries, sagas, or other managed workflows while maintaining safe serialization boundaries.
