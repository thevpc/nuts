package net.thevpc.nuts.boot.reserved.util;

import java.time.Instant;
import java.util.Objects;

public class NClockBoot {
    private final long timeMillis;
    private final long nanos;

    public static NClockBoot now() {
        return new NClockBoot(
                System.currentTimeMillis(),
                System.nanoTime()
        );
    }

//    @NMapBy
    public NClockBoot(
            /*@NMapBy(name = "timeMillis")*/ long timeMillis,
            /*@NMapBy(name = "nanos")*/ long nanos) {
        this.timeMillis = timeMillis;
        this.nanos = nanos;
    }

    public NDurationBoot minus(NClockBoot o) {
        return NDurationBoot.ofNanos(nanos - o.nanos);
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
        NClockBoot nClock = (NClockBoot) o;
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
