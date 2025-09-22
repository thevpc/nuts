---
title: Safe Locking with NLock
subTitle:  |
  <code>NLock</code> provides a flexible, high-level way to lock resources across threads
  and even processes. You can create locks from objects, paths, or resource IDs,
  and execute code safely while the lock is held. This ensures that critical sections
  are executed exclusively and consistently.
contentType: java
---

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
