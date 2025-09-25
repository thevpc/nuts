---
title: Progress Monitoring
---


Long-running or multi-step operations benefit from structured progress tracking. NProgressMonitor provides a flexible, hierarchical, and thread-safe way to track, report, and manage task progress. It supports splitting, weighting, cancellation, suspension, and undoing progress, making it suitable for both simple and complex workflows.


## Key features:
- Basic Progress Tracking – Update progress using setProgress(double progress) or as a fraction of total work setProgress(long current, long max). Events are emitted for start, progress, completion, undo, cancellation, and suspension.
- Hierarchical Progress – Split a monitor into subtasks using split(int count) or split(double... weights) to assign relative weights. This allows aggregation of multiple subtask progress into a single overall progress.
- Listeners & Reporting – Attach listeners via addListener(NProgressListener) to handle progress events programmatically. Output can be printed to streams or loggers using NProgressMonitors.of().ofPrintStream(...) or ofLogger(...).
- Structured Execution – runWith(Runnable) and runWithAll(Runnable...) integrate progress tracking into actual task execution. Each runnable can be associated with a subtask and progress weight.
- Indeterminate Progress – Supports tasks with unknown total duration using setIndeterminate().
- Estimated Duration – Automatically calculates elapsed and remaining time via getEstimatedRemainingDuration() and getEstimatedTotalDuration().
- Convenient Defaults – NProgressMonitors.of().of() returns the current monitor if one exists, or a silent fallback otherwise.
- ANSI-friendly Output – Works seamlessly with NText/NTextStyle for styled terminal output.

```java
NProgressMonitor monitor = NProgressMonitors.of().of(event -> {
    NOut.println(event);
});
monitor.setProgress(0);
monitor.setProgress(0.2);
monitor.setProgress(1.0);
monitor.complete();
```

## Example: Structured Subtasks

```java
NProgressMonitor monitor = NProgressMonitor.of(); // scoped monitor or silenced one
NProgressMonitor[] subtasks = monitor.split(3); // 3 weighted subtasks

subtasks[0].runWith(() -> doWork("Task A"));
subtasks[1].runWith(() -> doWork("Task B"));
subtasks[2].runWith(() -> doWork("Task C"));
```

## Example: Integration with Runnable Execution

```java
NProgressMonitor.of().runWithAll(
    tasks.stream()
         .map(task -> (Runnable) () -> processTask(task))
         .toArray(Runnable[]::new)
);
```

These capabilities make NProgressMonitor a robust tool to structure, visualize, and control progress in multi-step or parallel operations, including support for cancellation, suspension, and progress weighting.


## Terminal & ASCII Progress Rendering

In addition to structured progress monitoring, `NAF` allows rendering progress directly in the terminal, including an ASCII progress bar with messages:

```java
for (int i = 0; i < 100; i++) {
    Thread.sleep(100);
    NSession.of().getTerminal()
            .printProgress((i / 100f), NMsg.ofC("Processing item %s", i));
}
```
This will render a live progress bar with the associated message, updating in-place in the terminal. It works on both POSIX terminals and Windows terminals via Jansi, leveraging the same styling framework (NText/NTextStyle) used elsewhere.

## Integration with IO Streams

For monitoring IO progress, Nuts provides NInputStreamMonitor, which wraps an input stream to log or trace progress dynamically:

```java

NInputStreamMonitor monitor = NInputStreamMonitor.of()
        .setSource(new FileInputStream("/some/path"))
        .setLogProgress(true)
        .setTraceProgress(false);

NProgressListener listener= event->NOut.println(NMsg.of("progress : %s",event.getProgress()));

NInputSource monitoredSource = NInputSource.of(
        monitor.setProgressFactory(()->listener)
                .setLength(NPath.of("/some/path").length()) //estimated length
                .create()
);
// Reading the bytes will show progress in the console
byte[] bytes=monitoredSource.readBytes();

```

- NInputStreamMonitor wraps the source stream.
- NProgressListener receives events with progress information.
- setLength() allows estimating progress if the total size is known.
- This integrates seamlessly with the terminal output, leveraging NOut and NMsg for styled messages.
  
This demonstrates that progress monitoring in Nuts is not limited to computations—it can be applied to IO operations in a clean, observable way.
