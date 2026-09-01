---
title: Working with Workspaces and Session
subTitle:  |
  With NAF, what starts as a simple one-line setup can grow into a
  fully controlled runtime environment. Create multiple workspaces,
  configure sessions with custom flags, manage output formats, and
  orchestrate complex automation — all while keeping your code clean
  and portable. NAF gives you both simplicity for quick experiments
  and full power for advanced applications.
contentType: java
---

NWorkspace ws = Nuts.openWorkspace("--workspace=/path/to/ws");
ws.runWith(() -> {
    NSession.of()
    .copy()
    .dry(true)
    .outputFormat(NContentType.JSON)
    .runWith(() -> {
        NOut.println(NMaps.of("status", "ok"));
    });
});
