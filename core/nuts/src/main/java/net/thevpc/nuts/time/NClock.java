package net.thevpc.nuts.time;

import net.thevpc.nuts.elem.NMapBy;

import java.time.Instant;
import java.util.Objects;

public class NClock {
    private final long timeMillis;
    private final long nanos;

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

    public NDuration minus(NClock o) {
        return NDuration.ofNanos(nanos - o.nanos);
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(timeMillis);
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public long getTimeNanos() {
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
