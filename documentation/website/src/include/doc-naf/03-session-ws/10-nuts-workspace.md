---
title: NWorkspace
---

## Opening and Sharing Workspaces

### Default (global) workspace

```java
NWorkspace.require();
```

- Returns the currently shared (global) workspace if one exists.
- If no global workspace is present, creates and shares one by delegating to 

```java
Nuts.openWorkspace("--reset-options", "--in-memory").share();
```

This ensures that a workspace is always available, without requiring manual setup.

:::tip
This workspace is in-memory and ignores any inherited CLI options (--reset-options). 
It’s ideal for quick use cases, testing, or tools that need a minimal setup.
:::


## Scoped (local) workspace

If you need isolation or temporary workspace setup:


```java
Nuts.openWorkspace().runWith(() -> {
        // This code runs inside a thread-local scoped workspace
        // Nuts components here use the scoped context
        });
```

- Temporarily hides the global workspace inside the block,
- Scoped workspace is available in current and inherited threads,
- Ideal for frameworks, sandboxing, plugins and testing.
- Nuts.openWorkspace() uses persistent location (across processes) unless --in-memory is passed.


## Sharing workspace globally

To explicitly promote a workspace to global:

```java
Nuts.openWorkspace().share();
```

You may also customize it before sharing:

```java
Nuts.openWorkspace("--in-memory", "--color").share();
```

## Environment & System Info
Workspaces provide access to environment metadata:

NWorkspace ws = NWorkspace.current();


```java
ws.getHostName();               // Host name
ws.getPid();                    // Process ID
ws.getOsFamily();               // Linux, Windows, Mac, etc.
ws.getShellFamily();            // bash, cmd, powershell, etc.
ws.getPlatform();               // Java, Android, etc.
ws.getOs();                     // Full OS ID
ws.getOsDist();                 // OS distribution (e.g. Ubuntu)
ws.getArch();                   // CPU architecture (e.g. amd64)
ws.getArchFamily();            // Arch family (e.g. x86_64)
ws.getDesktopEnvironment();     // Gnome, KDE, etc.
ws.getDesktopEnvironmentFamily(); // Gnome-like, etc.
ws.isGraphicalDesktopEnvironment(); // true if graphical session
```

You can also list all available shell families or desktop environments:


```java
ws.getShellFamilies(); // e.g. [BASH, ZSH, CMD]
ws.getDesktopEnvironments(); // List of detected environments
```

## Accessing the Current Workspace

To retrieve the current NWorkspace (i.e., the one bound to the current thread context), you have two options:

## Elegant, fail-fast access

```java
NWorkspace ws = NWorkspace.of();
```

- Returns the current workspace if one is available,
- Throws an exception if no workspace is present,
- Recommended when a workspace is expected to exist.
- Equivalent to NWorkspace.get().get() but more expressive and fails clearly.

## Safe, optional access

```java
Optional<NWorkspace> wsOpt = NWorkspace.get();
```


- Returns an NOptional<NWorkspace>,
- Allows you to check for presence or fallback behavior,
- Useful in framework-level code or libraries that should not assume a workspace.

```java
NWorkspace.get().ifPresent(ws -> {
// do something with ws
});
```

:::tip
This does not create or share a workspace. It only retrieves one if it exists in the current context.
:::
