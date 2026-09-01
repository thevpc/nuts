---
title: Saga
---

`NSagaCallable` provides a structured way to define a series of steps with **automatic compensation** in case of failure.  
Each step can succeed or fail, and the saga system ensures that failed steps are undone according to the defined strategy.

This is useful for workflows that require atomicity across multiple independent operations, like distributed transactions, workflow orchestration, or resilient pipelines.

### Example 1: Simple Saga with failure and compensation

```java
@Test
public void testSaga() {
    NSagaCallable<Object> saga = NConcurrent.of().sagaCallBuilder()
            .start()
            .then("step 1", MyNSagaStep.asSuccessful(1))
            .then("step 2", MyNSagaStep.asSuccessful(2))
            .then("step 3", MyNSagaStep.asErroneous(3))
            .then("step 4", MyNSagaStep.asSuccessful(4))
            .end().build();

    saga.call();
}

private static class MyNSagaStep implements NSagaStep {
    String name;
    boolean err;

    public MyNSagaStep(String name, boolean err) {
        this.name = name;
        this.err = err;
    }

    public static MyNSagaStep asSuccessful(int name) {
        return new MyNSagaStep("step " + name, false);
    }

    public static MyNSagaStep asErroneous(int name) {
        return new MyNSagaStep("step " + name, true);
    }

    @Override
    public Object call(NSagaContext context) {
        if (err) {
            NErr.println(Instant.now() + " : err call " + name);
            throw new NIllegalStateException(NMsg.ofC("unexpected error at %s", name));
        } else {
            NOut.println(Instant.now() + " : call " + name);
        }
        return name;
    }

    @Override
    public void undo(NSagaContext context) {
        NOut.println(Instant.now() + " : undo " + name);
    }
}
```

### Example 2: Conditional Saga

```java
NSagaCallable<Object> conditionalSaga = NConcurrent.of().sagaCallBuilder()
        .start()
        .then("step 1", MyNSagaStep.asSuccessful(1))
        .thenIf("conditional step", ctx -> ctx.getVar("shouldRun") != null && (boolean)ctx.getVar("shouldRun"))
        .then("step 2", MyNSagaStep.asSuccessful(2))
        .otherwise()
        .then("step 3", MyNSagaStep.asSuccessful(3))
        .end()
        .build();

conditionalSaga.call();
```

### Example 3: Saga with While Loop

```java
NSagaCallable<Object> loopSaga = NConcurrent.of().sagaCallBuilder()
        .start()
        .then("init counter", ctx -> {
            ctx.setVar("counter", 0);
            NOut.println("Counter initialized");
            return null;
        })
        .thenWhile("loop while counter < 3", ctx -> (int)ctx.getVar("counter") < 3)
            .then("increment counter", ctx -> {
                int c = (int) ctx.getVar("counter");
                ctx.setVar("counter", c + 1);
                NOut.println("Counter incremented to " + (c + 1));
                return null;
            })
        .end()
        .then("final step", ctx -> {
            NOut.println("Final counter value: " + ctx.getVar("counter"));
            return null;
        })
        .end()
        .build();

loopSaga.call();
```

### Key Interfaces

* `NSagaCallable<T>`: Represents the full saga callable.
* `NSagaStep`: A single step, with call and undo.
* `NSagaContext`: Context for storing variables and passing data between steps.
* `NSagaCallableBuilder`: Fluent builder for defining sagas.
* `NSagaCondition`: Conditional branching in a saga (for thenIf or thenWhile).
* `NSagaStore`: Optional persistence for saga state.

### Status and Node Enums

* `NSagaNodeStatus`: Status of a node (PENDING, RUNNING, FINISHED, FAILED, COMPENSATING, etc.).
* `NSagaStatus`: Overall saga status (PENDING, RUNNING, SUCCESS, ROLLED_BACK, PARTIAL_ROLLBACK, FAILED).

### Notes

* `Execution vs Definition`: Building a saga only defines the workflow. Steps are executed when call() is invoked.
* `Automatic Compensation`: If a step fails, prior executed steps are automatically undone using their undo methods.
* `Variable Sharing`: Use NSagaContext to store and access variables across steps.
* `Persistence`: Implement NSagaStore to persist saga state for long-running workflows or distributed systems.
* `Step Status`: Use NSagaCallable.status() to inspect progress, including compensations in progress.
