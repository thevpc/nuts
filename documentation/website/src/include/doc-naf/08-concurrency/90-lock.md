---
title: Persistent Locks
---


`NLock` provides a flexible, high-level way to lock resources across threads
and even processes. You can create locks from objects, paths, or resource IDs,
and execute code safely while the lock is held. This ensures that critical sections
are executed exclusively and consistently.


```java
// Example 1: Create a lock from an object
NLock lock = NLock.ofPath(NPath.of("/path/to/resource.txt");

// Check lock status
Nout.println("Is locked? " + lock.isLocked());

// Run code while holding the lock
        lock.runWith(() -> {
        NOut.println("Executing critical section...");
});

// Check if current thread holds the lock
        Nout.println("Held by current thread? " + lock.isHeldByCurrentThread());

```

`NLock` also supports locks tied to workspace resources or IDs, allowing
inter-process synchronization. You can execute tasks immediately, with a timeout,
or safely retrieve results using `callWith`.

```java
NId resourceId = NId.of("net.thevpc.nuts:nuts#0.8.6");
// Lock tied to workspace resource
NLock idLock = NLock.ofIdPath(resourceId);

// Execute a task and get result
// The lock is process-safe and prevents other processes from entering the critical section
String result = idLock.callWith(() -> {
    NOut.println("Working with locked resource...");
    return "done";
}, 5, TimeUnit.SECONDS).orNull();

NOut.println("Result = " + result);

// Run immediately if lock is free
boolean executed = idLock.runWithImmediately(() -> {
    NOut.println("Quick task executed under lock");
});
NOut.println("Was executed? " + executed);
```
