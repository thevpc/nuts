---
title: Extensions
---

## Nuts Extension Mechanism
Nuts provides a dynamic, score-driven Service Provider Interface (SPI) framework. It enables runtime discovery, context-sensitive implementation resolution, and transparent overrides of framework subsystems.

### 1. System Architecture
```
+-------------------------------------------------------------------------+
|                              NWorkspace                                 |
|                                                                         |
|  +-------------------------------------------------------------------+  |
|  |                           NExtensions                             |  |
|  |  +-------------------------+     +-----------------------------+  |  |
|  |  | Extension Lifecycle     |     | Component Registry          |  |  |
|  |  | - loadExtension(NId)    |     | - registerType(...)         |  |  |
|  |  | - unloadExtension(NId)  |     | - registerInstance(...)     |  |  |
|  |  +-------------------------+     +-----------------------------+  |  |
|  |  +-------------------------+     +-----------------------------+  |  |
|  |  | Scoring Engine          |     | Instantiation & Resolution  |  |  |
|  |  | - NScore evaluation     |     | - createSupported(...)      |  |  |
|  |  | - NScoredValue metadata |     | - createAllSupported(...)   |  |  |
|  |  +-------------------------+     +-----------------------------+  |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```


#### Key Capabilities & API Methods

- Extension Point: Any interface inheriting from net.thevpc.nuts.spi.NComponent.
- SPI Registration: All extensions register via a single service file: META-INF/services/net.thevpc.nuts.spi.NComponent.
- Registration Lifecycle: When a core or plugin JAR is loaded, its extensions are indexed in a one-pass scan into an in-memory registry managed by NExtensions.
- Resolution Engine: Evaluates candidate implementations against an NScorableContext and instantiates the candidate with the highest positive score. Ties default to registration order (first discovered wins).

#### Resolution & Factory Queries

- Single Best Match: NExtensions.of(NEnv.class) or createSupported(type, criteria) resolves the highest-scoring candidate (score > 0).
- Multi-Match / Chain Execution: createAllSupported(type, criteria) returns a list of all candidates with positive scores, sorted in descending order of precedence.
- Bulk Retrieval: createAll(type) returns instances of all registered types implementing that extension point.

#### Dynamic Lifecycle Management

-    Runtime Loading: loadExtension(NId id) pulls new extensions into the workspace classpath and indexes their types.
-    Hot Unloading: unloadExtension(NId id) detaches classes and instances associated with that artifact source.

### 2. Scoring System (`NScorable`)

Every candidate is evaluated against runtime criteria before instantiation.

| Score Level       | Constant                        | Value      | Role                                        |
|:------------------|:--------------------------------|:-----------|:--------------------------------------------|
| Custom            | `NScorable.CUSTOM_SCORE`        | 1000+      | User-defined overrides (takes priority)     |
| Default           | `NScorable.DEFAULT_SCORE`       | 10         | Standard built-in implementations           |
| Unsupported       | `NScorable.UNSUPPORTED_SCORE`   | -1 (<= 0)  | Candidate is discarded for current context  |

### 3. Component Scopes (`NScopeType`)

Components define their lifecycle using the @NComponentScope annotation:

| Scope               | Behavior                                                             | Typical Use Case                                                                 | 
|:--------------------|:---------------------------------------------------------------------|:---------------------------------------------------------------------------------|
| WORKSPACE           | One instance per NWorkspace reference.                               | Heavy tooling, format executors (ZipExecutorComponent), thread pools.            | 
| SESSION             | One instance per NSession. Isolated to a specific session execution. | Session-specific caches, localized configuration.                                | 
| SHARED_SESSION      | Shared across a session and all sub-sessions/copies derived from it. | Shared state across child commands or task graphs.                               |
| TRANSITIVE_SESSION  | Copied/cloned when a session is copied.                              | Session configurations that start with parent state but isolate child mutations. |
| PROTOTYPE (Default) | A fresh instance is created on every resolution call.                | Stateful command builders, short-lived task processors.                          |


### 4 API, SPI, & RPI Specification
Nuts unifies all runtime components under the NComponent interface while distinguishing three functional tiers: API, SPI, and RPI.

#### 1. The Three Component Tiers

| Tier    | Name                              | Resolution Mechanism                              | Role                                                                                                 | Example                                 |
|:--------|:----------------------------------|:--------------------------------------------------|------------------------------------------------------------------------------------------------------|-----------------------------------------|
| API     | Application Programming Interface | Static entry points (NEnv.of(), NEnv.get())       | Public contracts consumed by applications and commands.                                              | NEnv, NSession, NWorkspace              | 
| SPI     | Service Provider Interface        | Dynamic discovery via manifest & scoring pipeline | Pluggable strategies, protocol extensions, custom engines.                                           | NLogFactorySPI, NPathFactorySPI         | 
| RPI     | Reserved Programming Interface    | Fast-path hard-wired resolution in NExtensions    | Essential runtime internals soldered by the core engine. Non-overridable. never used by applications | NTextRPI, internal session coordinators | 

#### 2. Internal Resolution Engine Mechanics

When `NExtensions.of(Type.class)` or `NExtensions.createSupported(Type.class, criteria)` is invoked, `NExtensions` executes one of two paths:

##### Path A: The RPI Fast-Path (Hard-Wired & Scoped)
For core interfaces, the implementation is explicitly wired to avoid reflection and dynamic scanning.
##### Path B: The SPI Dynamic Scoring Pipeline
For pluggable extension points, NExtensions scans registered classes, evaluates support levels, and lazily instantiates the highest-scoring candidate.

#### 3. How APIs Consume RPIs
Public API convenience methods use RPIs as internal engines without exposing internal implementation details:
```java
public interface NObjectWriter extends NCmdLineConfigurable, NComponent {
    
    // API static factory delegates to RPI singleton in the current session
    static NOptional<NObjectWriter> get(Object any) {
        return NTextRPI.of().createWriter(any);
    }
}
```

### 5. Implementation Declaration Patterns

#### Pattern A: Static Score Annotation (Class-Level)

Best for unconditional singletons, adapters, or service providers.

```java
@NScore(fixed = NScorable.CUSTOM_SCORE)
public class Slf4JNLogFactorySPI implements NLogFactorySPI {

    public Slf4JNLogFactorySPI() {
        // Default constructor resolved automatically
    }

    @Override
    public NLogSPI getLogSPI(String name) {
        return new Slf4JNLogSPI(name);
    }
}
```

#### Pattern B: Dynamic Context Scoring (Method-Level)
Best for transport protocols, OS-specific hooks, or format decoders. Any public static method annotated with @NScore returning int is evaluated.


```java
@NComponentScope(NScopeType.PROTOTYPE)
public class NEnvSshImpl implements NEnv {
    public static final String PROTOCOL = "ssh";

    // Priority 1: Context constructor
    public NEnvSshImpl(NScorableContext context) {
        NConnectionString conn=context.criteria();
        //...
    }

    // Dynamic support evaluation
    @NScore
    public static int checkSshSupport(NScorableContext context) {
        NConnectionString conn = context.criteria(NConnectionString.class);
        if (conn != null && PROTOCOL.equalsIgnoreCase(conn.protocol())) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
```

### 4. Constructor & Method Resolution Rules
- Scoring Methods:
  - Must be `public static int <anyName>(NScorableContext context)`.
  - If multiple `@NScore` methods exist on a single class, all are evaluated.
- Constructor Precedence:
  - `public MyImpl(NScorableContext context)` (preferred if context is needed).
  - `public MyImpl() (default no-arg constructor)`.

### 5. Runtime Usage

#### Case 1: Resolving a Singleton / Factory (e.g., Logger)
```java
// Automatic resolution of highest-priority factory (e.g., Slf4J over java.util.logging)
NLogFactorySPI loggerFactory = NExtensions.of(NLogFactorySPI.class);
NLogSPI logger = loggerFactory.getLogSPI("AppLogger");
```

#### Case 2: Resolving a Context-Dependent Provider (e.g., Transport Env)
```java
NConnectionString conn = NConnectionString.of("ssh://user@remote-host");

// createSupported injects criteria into NScorableContext
NOptional<NEnv> env = NExtensions.of().createSupported(NEnv.class, conn);
```

#### Case 3: Pipeline / Filter Processing (All Supporting)
```java
// Discover and execute all interceptors/validators supporting this input
List<NCommandValidator> validators = NExtensions.of()
    .createAllSupported(NCommandValidator.class, currentCommand);

for (NCommandValidator validator : validators) {
    validator.validate(currentCommand);
}
```

#### Case 4: Programmatic Registration
```java
// Dynamic in-code registration of custom providers
NExtensions.of().registerType(NEnv.class, CustomDockerEnvImpl.class, NId.of("com.myorg:docker-ext#1.0.0").get());
```