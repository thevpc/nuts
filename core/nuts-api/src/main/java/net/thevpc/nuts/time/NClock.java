package net.thevpc.nuts.time;

import net.thevpc.nuts.elem.NMapBy;

import java.time.Instant;
import java.util.Objects;

/**
 * NClock class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NClock {
    private final long timeMillis;
    private final long nanos;

    /**
     * Now.
     *
     * @return now result
     */
    public static NClock now() {
        return new NClock(
                System.currentTimeMillis(),
                System.nanoTime()
        );
    }

    @NMapBy
    public NClock(
            @NMapBy(name = "timeMillis") long timeMillis,
            @NMapBy(name = "nanos") long nanos) {
        this.timeMillis = timeMillis;
        this.nanos = nanos;
    }

    /**
     * Minus.
     *
     * @param o o
     * @return minus result
     */
    public NDuration minus(NClock o) {
        return NDuration.ofNanos(nanos - o.nanos);
    }

    /**
     * Instant.
     *
     * @return instant result
     */
    public Instant instant() {
        return Instant.ofEpochMilli(timeMillis);
    }

    /**
     * Time millis.
     *
     * @return time millis result
     */
    public long timeMillis() {
        return timeMillis;
    }

    /**
     * Time nanos.
     *
     * @return time nanos result
     */
    public long timeNanos() {
        return nanos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NClock nClock = (NClock) o;
        return timeMillis == nClock.timeMillis && nanos == nClock.nanos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeMillis, nanos);
    }

    @Override
    public String toString() {
        return "NutsClock{" +
                "timeMillis=" + timeMillis +
                ", timeNanos=" + nanos +
                '}';
    }
}
