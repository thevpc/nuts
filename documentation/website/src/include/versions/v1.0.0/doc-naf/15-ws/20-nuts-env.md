---
title: NEnv
---

## Environment & System Info
Environments provide access to environment metadata (bound to the workspace):

NEnv env = NEnv.of();


```java
env.hostName();               // Host name
env.pid();                    // Process ID
env.osFamily();               // Linux, Windows, Mac, etc.
env.shellFamily();            // bash, cmd, powershell, etc.
env.platform();               // Java, Android, etc.
env.os();                     // Full OS ID
env.osDist();                 // OS distribution (e.g. Ubuntu)
env.arch();                   // CPU architecture (e.g. amd64)
env.archFamily();            // Arch family (e.g. x86_64)
env.desktopEnvironment();     // Gnome, KDE, etc.
env.desktopEnvironmentFamily(); // Gnome-like, etc.
env.graphicalDesktopEnvironment(); // true if graphical session
```

You can also list all available shell families or desktop environments:


```java
env.shellFamilies(); // e.g. [BASH, ZSH, CMD]
env.desktopEnvironments(); // List of detected environments
```

