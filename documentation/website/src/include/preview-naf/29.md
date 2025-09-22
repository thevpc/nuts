---
title: Advanced Locking and Timed Execution
subTitle:  |
  <code>NLock</code> also supports locks tied to workspace resources or IDs, allowing
  inter-process synchronization. You can execute tasks immediately, with a timeout,
  or safely retrieve results using <code>callWith</code>.
contentType: java
---

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
