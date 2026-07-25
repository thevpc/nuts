package net.thevpc.nuts.mon;

import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;

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
