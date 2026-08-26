---
title: NAF SLF4J Integration
---

# Nuts SLF4J Integration

Nuts provides seamless integration with SLF4J, the standard logging facade for Java applications. This integration allows you to use Nuts' powerful `NLog` structured logging system while maintaining compatibility with your existing SLF4J infrastructure.

## Installation

Add the following dependency to your Spring Boot project:

```xml
<dependency>
    <groupId>net.thevpc.nuts</groupId>
    <artifactId>nuts-nuts-slf4j</artifactId>
    <version>{{runtimeVersion}}</version>
</dependency>
```

## Why SLF4J Integration?

SLF4J is the de facto standard logging facade in the Java ecosystem. By integrating Nuts with SLF4J, you get:

- **Unified logging:** Use `NLog` alongside your existing SLF4J loggers without conflicts
- **Flexible backend support:** Route logs to Logback, Log4j2, or any SLF4J-compatible backend
- **Structured logging:** Leverage Nuts' `NLog` for rich, semantically meaningful logs with `NMsg`
- **Backward compatibility:** Existing SLF4J code continues to work without modification
- **Context propagation:** MDC (Mapped Diagnostic Context) values flow seamlessly between Nuts and SLF4J

## Basic Usage

Once the dependency is added, `NLog` automatically delegates to SLF4J:

```java
import net.thevpc.nuts.NLog;
import net.thevpc.nuts.NMsg;

@Component
public class MyService {
    private static final NLog log = NLog.of(MyService.class);
    
    public void processData(String data) {
        log.info(NMsg.ofC("Processing data: %s", data));
        try {
            // ... processing logic
            log.debug(NMsg.ofC("Data processing completed successfully"));
        } catch (Exception ex) {
            log.error(NMsg.ofC("Failed to process data: %s", data)
                .withThrowable(ex)
            );
        }
    }
}
```

## Structured Logging with NLog and SLF4J

The true power emerges when combining `NLog` with `NMsg` for structured, context-aware logging:

```java
@Component
public class OrderProcessor {
    private static final NLog log = NLog.of(OrderProcessor.class);
    
    public void processOrder(Order order) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info(NMsg.ofC("Processing order #%s from customer %s", order.getId(), order.getCustomerId())
                .withIntent(NMsgIntent.START)
            );
            
            // ... order processing
            
            long duration = System.currentTimeMillis() - startTime;
            log.info(NMsg.ofC("Order #%s completed successfully", order.getId())
                .withIntent(NMsgIntent.SUCCESS)
                .withDurationMs(duration)
            );
        } catch (PaymentException ex) {
            log.error(NMsg.ofC("Payment failed for order #%s", order.getId())
                .withIntent(NMsgIntent.FAIL)
                .withThrowable(ex)
            );
            throw ex;
        }
    }
}
```

## Using NMsg Formatting Styles

The integration supports all `NMsg` formatting styles and automatically translates them for SLF4J:

```java
private static final NLog log = NLog.of(MyClass.class);

// C-style formatting (printf-like)
log.info(NMsg.ofC("User %s logged in from %s", username, ipAddress));

// J-style formatting (Java Logging style)
log.warn(NMsg.ofJ("Configuration file not found: {0}", configPath));

// Variable-based formatting with placeholders
log.debug(NMsg.ofV("Cache miss for $key with TTL $ttl seconds",
    NMaps.of("key", cacheKey, "ttl", ttlSeconds)
));
```

## Semantic Logging with Intents

Attach semantic meaning to logs using `NMsgIntent` for better filtering, monitoring, and analysis:

```java
// Operational events
log.info(NMsg.ofC("Service started on port %d", port)
    .withIntent(NMsgIntent.START)
);

log.info(NMsg.ofC("Database connection established")
    .withIntent(NMsgIntent.SUCCESS)
);

// Data operations
log.debug(NMsg.ofC("Reading user record: %s", userId)
    .withIntent(NMsgIntent.READ)
);

log.info(NMsg.ofC("User profile updated for %s", userId)
    .withIntent(NMsgIntent.UPDATE)
);

// Resource management
log.info(NMsg.ofC("Cache entry added for key: %s", key)
    .withIntent(NMsgIntent.ADD)
);

log.info(NMsg.ofC("Temporary file removed: %s", tempFile)
    .withIntent(NMsgIntent.REMOVE)
);

// Failure handling
log.error(NMsg.ofC("Retry attempt %d failed", attemptNumber)
    .withIntent(NMsgIntent.FAIL)
    .withThrowable(exception)
);
```

## Scoped Logging in Spring Components

Use scoped logging to apply context across multiple Spring beans and method calls:

```java
@Service
public class RequestHandler {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    public void handleRequest(RequestContext context) {
        // Set up a logging scope for this request
        NLogs.of().runWith(
            NLogContext.of()
                .withMessagePrefix(NMsg.ofC("[Request %s]", context.getRequestId()))
                .withPlaceholder("userId", context.getUserId())
                .withPlaceholder("sessionId", context.getSessionId()),
            () -> {
                userService.validateUser();      // Logs inherit request context
                orderService.processOrder();      // Logs inherit request context
            }
        );
    }
}

@Service
public class UserService {
    private static final NLog log = NLog.of(UserService.class);
    
    public void validateUser() {
        // This log automatically includes the request prefix and placeholders
        log.info(NMsg.ofV("Validating user $userId in session $sessionId"));
    }
}
```

## Integration with Spring Boot Logging Configuration

By default, SLF4J routes Nuts logs through your Spring Boot logging backend (Logback, Log4j2, etc.). Configure your `application.yml` or `logback-spring.xml` as usual:

```yaml
logging:
  level:
    net.thevpc.nuts: DEBUG
    com.myapp: INFO
  file:
    name: logs/application.log
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

Nuts logs will respect these configurations automatically, allowing you to manage Nuts logging alongside your application's other loggers.

## Best Practices

**Use NLog for internal diagnostics:** `NLog` is designed for developer-focused, structured diagnostics. Use it for tracing operations, debugging, and understanding application behavior.

**Use NOut for user-facing output:** Reserve `NOut` and `NTrace` for messages intended for end users or CLI output.

**Attach semantic verbs:** Always use `NMsgIntent` to classify your logs. This enables powerful filtering, monitoring, and analysis downstream.

**Combine with Spring AOP for cross-cutting concerns:** Use Spring's AOP and Nuts scoped logging for consistent request/transaction tracing:

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Before("@annotation(com.myapp.Traced)")
    public void beforeTracedMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        NLog.of(joinPoint.getTarget().getClass())
            .info(NMsg.ofC("Entering method: %s", methodName)
                .withIntent(NMsgIntent.START)
            );
    }
}
```

## Learn More

For comprehensive details on `NLog` and `NMsg`, see the [NLog for elegant Logging](./nlog-guide) documentation, which covers advanced features like custom log handlers, MDC integration, and output formatting.

## Summary

The Nuts SLF4J integration bridges the gap between Nuts' powerful structured logging capabilities and the Java ecosystem's standard logging facade. By combining `NLog`, `NMsg`, and SLF4J, you gain:

- Rich, semantic logging with intents and structured messages
- Full compatibility with existing SLF4J infrastructure
- Scoped logging for contextual diagnostics
- Flexible output routing and configuration
- Seamless integration with Spring Boot applications
