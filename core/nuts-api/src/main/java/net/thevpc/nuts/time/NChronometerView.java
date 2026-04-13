package net.thevpc.nuts.time;

import java.time.temporal.ChronoUnit;

public interface NChronometerView {
    String name();
    boolean isStarted();
    boolean isStopped();
    boolean isSuspended();
    NClock startClock();
    NClock endClock();
    NDuration duration();
    long durationMs();
    long durationNanos();
    ChronoUnit smallestUnit();
    ChronoUnit largestUnit();
    String toString(NDurationFormatMode mode);
}
