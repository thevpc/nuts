package net.thevpc.nuts.boot.reserved.util;

import java.time.Instant;
import java.util.Objects;

public class NBootClock {
    private final long timeMillis;
    private final long nanos;

    public static NBootClock now() {
        return new NBootClock(
                System.currentTimeMillis(),
                System.nanoTime()
        );
    }

//    @NMapBy
    public NBootClock(
            /*@NMapBy(name = "timeMillis")*/ long timeMillis,
            /*@NMapBy(name = "nanos")*/ long nanos) {
        this.timeMillis = timeMillis;
        this.nanos = nanos;
    }

    public NBootDuration minus(NBootClock o) {
        return NBootDuration.ofNanos(nanos - o.nanos);
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
        NBootClock nClock = (NBootClock) o;
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
