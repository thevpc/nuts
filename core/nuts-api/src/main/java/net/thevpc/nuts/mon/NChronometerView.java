package net.thevpc.nuts.mon;

import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;

import java.time.temporal.ChronoUnit;

/**
 * NChronometerView interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NChronometerView {
    /**
     * Name.
     *
     * @return name result
     */
    String name();
    /**
     * Checks if is started.
     *
     * @return is started result
     */
    boolean isStarted();
    /**
     * Checks if is stopped.
     *
     * @return is stopped result
     */
    boolean isStopped();
    /**
     * Checks if is suspended.
     *
     * @return is suspended result
     */
    boolean isSuspended();
    /**
     * Start clock.
     *
     * @return start clock result
     */
    NClock startClock();
    /**
     * End clock.
     *
     * @return end clock result
     */
    NClock endClock();
    /**
     * Duration.
     *
     * @return duration result
     */
    NDuration duration();
    /**
     * Duration ms.
     *
     * @return duration ms result
     */
    long durationMs();
    /**
     * Duration nanos.
     *
     * @return duration nanos result
     */
    long durationNanos();
    /**
     * Smallest unit.
     *
     * @return smallest unit result
     */
    ChronoUnit smallestUnit();
    /**
     * Largest unit.
     *
     * @return largest unit result
     */
    ChronoUnit largestUnit();
    String toString(NDurationFormatMode mode);
}
