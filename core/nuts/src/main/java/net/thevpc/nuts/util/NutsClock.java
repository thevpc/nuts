package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NutsMapBy;

import java.time.Instant;
import java.util.Objects;

public class NutsClock {
    private final long timeMillis;
    private final long nanos;

    public static NutsClock now() {
        return new NutsClock(
                System.currentTimeMillis(),
                System.nanoTime()
        );
    }

    @NutsMapBy
    public NutsClock(
            @NutsMapBy(name = "timeMillis") long timeMillis,
            @NutsMapBy(name = "nanos") long nanos) {
        this.timeMillis = timeMillis;
        this.nanos = nanos;
    }

    public NutsDuration minus(NutsClock o) {
        return NutsDuration.ofNanos(nanos - o.nanos);
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(timeMillis);
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public long getNanos() {
        return nanos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsClock nutsClock = (NutsClock) o;
        return timeMillis == nutsClock.timeMillis && nanos == nutsClock.nanos;
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
