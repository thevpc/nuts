---
title: Progress Monitoring with NProgressMonitor
subTitle:  |
  NProgressMonitor provides a flexible and context-aware way to track
  progress in tasks. You can create incremental, split, and nested
  monitors, log events to any output, and automatically propagate the
  current progress context using NProgressMonitor.of(). This allows
  embedded or nested operations to report progress seamlessly without
  explicit parameter passing.
contentType: java
---

// 1. Simple incremental progress
NProgressMonitor mon = NProgressMonitors.of().ofSysOut();
mon.start();
for (int i = 0; i < 10; i++) {
    mon.setProgress(i * 1.0 / 10, NMsg.ofC("Step %.1f", i * 1.0 / 10));
}
mon.complete();

// 2. Split progress for subtasks
NProgressMonitor[] split = mon.split(2);
split[0].incremental(5).complete();
split[1].incremental(5).complete();

// 3. Context-aware progress
NProgressHandler handler = event -> NOut.println(event);
NProgressMonitors.of().of(handler).runWith(() -> {
    NProgressMonitor inner = NProgressMonitor.of();
    inner.start();
    inner.setProgress(0.5, NMsg.ofC("Halfway done"));
    inner.complete();
});
