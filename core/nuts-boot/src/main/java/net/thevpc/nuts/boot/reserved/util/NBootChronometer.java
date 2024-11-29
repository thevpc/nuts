/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.boot.reserved.util;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author taha.bensalah@gmail.com
 */
public class NBootChronometer implements Serializable {

    private final static long serialVersionUID = 1L;
    private long accumulatedNanos;
    private NBootClock startClock;
    private NBootClock endClock;
    private String name;
    private long lastNanos;
    private boolean running;
    private ChronoUnit smallestUnit;
    private ChronoUnit largestUnit;

    public static NBootChronometer startNow() {
        return startNow(null, null);
    }

    public static NBootChronometer startNow(String name) {
        return startNow(name, null);
    }

    public static NBootChronometer startNow(ChronoUnit smallestUnit) {
        return startNow(null, smallestUnit);
    }

    public static NBootChronometer startNow(String name, ChronoUnit smallestUnit) {
        return new NBootChronometer(name, smallestUnit).start();
    }

    public NBootChronometer() {
    }

    public NBootChronometer copy() {
        return new NBootChronometer(
                name, startClock, endClock, accumulatedNanos, lastNanos, running, smallestUnit, largestUnit
        );
    }

    public NBootChronometer(
             String name,
             NBootClock startClock,
             NBootClock endClock,
             long accumulatedNanos,
             long lastNanos,
             boolean running,
             ChronoUnit smallestUnit,
             ChronoUnit largestUnit) {
        this.accumulatedNanos = accumulatedNanos;
        this.startClock = startClock;
        this.endClock = endClock;
        this.name = name;
        this.lastNanos = lastNanos;
        this.running = running;
        this.smallestUnit = smallestUnit;
        this.largestUnit = largestUnit;
    }

    /**
     * restart chronometer and returns a stopped snapshot/copy of the current
     *
     * @return {@code this} instance
     */
    public NBootChronometer restart() {
        stop();
        NBootChronometer c = copy();
        start();
        return c;
    }

    /**
     * restart chronometer with new name and returns a stopped snapshot/copy of
     * the current (with old name)
     *
     * @param newName newName
     * @return {@code this} instance
     */
    public NBootChronometer restart(String newName) {
        stop();
        NBootChronometer c = copy();
        setName(newName);
        start();
        return c;
    }

    public NBootChronometer(String name) {
        this.name = name;
    }

    public NBootChronometer(String name, ChronoUnit smallestUnit) {
        this.name = name;
        this.smallestUnit = smallestUnit;
    }

    public NBootChronometer setName(String desc) {
        this.name = desc;
        return this;
    }

    public NBootChronometer updateDescription(String desc) {
        setName(desc);
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return startClock != null;
    }

    public boolean isStopped() {
        return endClock != null;
    }

    public NBootChronometer reset() {
        endClock = null;
        startClock = null;
        lastNanos = 0;
        accumulatedNanos = 0;
        running = false;
        return this;
    }

    public NBootChronometer start() {
        endClock = null;
        startClock = NBootClock.now();
        lastNanos = startClock.getTimeNanos();
        accumulatedNanos = 0;
        running = true;
        return this;
    }

    public NBootChronometer accumulate() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = n;
        }
        return this;
    }

    public Duration lap() {
        if (running) {
            long n = System.nanoTime();
            long lapValue = n - lastNanos;
            this.accumulatedNanos += lapValue;
            lastNanos = n;
            return Duration.ofNanos(lapValue);
        }
        return Duration.ZERO;
    }

    public boolean isSuspended() {
        return !running;
    }

    public NBootChronometer suspend() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NBootChronometer resume() {
        if (!running) {
            lastNanos = System.nanoTime();
            running = true;
        }
        return this;
    }

    public NBootChronometer stop() {
        if (running) {
            endClock = NBootClock.now();
            accumulatedNanos += endClock.getTimeNanos() - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NBootClock getStartClock() {
        return startClock;
    }

    public NBootClock getEndClock() {
        return endClock;
    }

    public NBootDuration getDuration() {
        return NBootDuration.ofNanos(getDurationNanos(), getSmallestUnit(), getLargestUnit());
    }

    public long getDurationMs() {
        return getDurationNanos() / 1000000L;
    }

    public long getDurationNanos() {
        if (startClock == null) {
            return 0;
        }
        if (running) {
            long curr = System.nanoTime() - lastNanos;
            return (curr + accumulatedNanos);
        }
        return accumulatedNanos;
    }

    public String toString() {
        String s = name == null ? "" : name + "=";
        return s + getDuration().toString();
    }

    public ChronoUnit getSmallestUnit() {
        return smallestUnit;
    }

    public NBootChronometer setSmallestUnit(ChronoUnit smallestUnit) {
        this.smallestUnit = smallestUnit;
        return this;
    }

    public ChronoUnit getLargestUnit() {
        return largestUnit;
    }

    public NBootChronometer setLargestUnit(ChronoUnit largestUnit) {
        this.largestUnit = largestUnit;
        return this;
    }

}
