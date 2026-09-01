---
title: NExec & NPs — Unified Process Execution
subTitle:  |
  NAF offers a complete abstraction for process execution and
  monitoring. With NExec, you can run system commands, remote jobs, or
  even Maven artifacts seamlessly. With NPs, you can introspect and
  control running processes across platforms. Together, they form a
  portable, workspace-aware execution toolkit that abstracts OS
  differences, integrates with Nuts’ runtime, and makes both starting
  and supervising processes trivial — whether locally or remotely.
contentType: java
---

    // Run a system command and capture output
    String out = NExec.of("ls").system().grabAll().run().getGrabbedOutString();

    // Run a Maven artifact (auto-resolves if missing)
    NExec.of("netbeans-launcher").run();

    // List all Java processes on the local machine
    NPs.of().setPlatformFamily(NPlatformFamily.JAVA)
           .getResultList()
           .forEach(NOut::println);

    // Kill a process by ID (if supported on the platform)
    NPs.of().killProcess("12345");

    // Inspect processes on a remote host
    NPs.of().at("ssh://myuser@myserver").getResultList();
