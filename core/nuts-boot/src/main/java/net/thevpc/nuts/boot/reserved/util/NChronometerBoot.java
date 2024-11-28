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
public class NChronometerBoot implements Serializable {

    private final static long serialVersionUID = 1L;
    private long accumulatedNanos;
    private NClockBoot startClock;
    private NClockBoot endClock;
    private String name;
    private long lastNanos;
    private boolean running;
    private ChronoUnit smallestUnit;
    private ChronoUnit largestUnit;

    public static NChronometerBoot startNow() {
        return startNow(null, null);
    }

    public static NChronometerBoot startNow(String name) {
        return startNow(name, null);
    }

    public static NChronometerBoot startNow(ChronoUnit smallestUnit) {
        return startNow(null, smallestUnit);
    }

    public static NChronometerBoot startNow(String name, ChronoUnit smallestUnit) {
        return new NChronometerBoot(name, smallestUnit).start();
    }

    public NChronometerBoot() {
    }

    public NChronometerBoot copy() {
        return new NChronometerBoot(
                name, startClock, endClock, accumulatedNanos, lastNanos, running, smallestUnit, largestUnit
        );
    }

    public NChronometerBoot(
             String name,
             NClockBoot startClock,
             NClockBoot endClock,
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
    public NChronometerBoot restart() {
        stop();
        NChronometerBoot c = copy();
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
    public NChronometerBoot restart(String newName) {
        stop();
        NChronometerBoot c = copy();
        setName(newName);
        start();
        return c;
    }

    public NChronometerBoot(String name) {
        this.name = name;
    }

    public NChronometerBoot(String name, ChronoUnit smallestUnit) {
        this.name = name;
        this.smallestUnit = smallestUnit;
    }

    public NChronometerBoot setName(String desc) {
        this.name = desc;
        return this;
    }

    public NChronometerBoot updateDescription(String desc) {
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

    public NChronometerBoot reset() {
        endClock = null;
        startClock = null;
        lastNanos = 0;
        accumulatedNanos = 0;
        running = false;
        return this;
    }

    public NChronometerBoot start() {
        endClock = null;
        startClock = NClockBoot.now();
        lastNanos = startClock.getTimeNanos();
        accumulatedNanos = 0;
        running = true;
        return this;
    }

    public NChronometerBoot accumulate() {
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

    public NChronometerBoot suspend() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NChronometerBoot resume() {
        if (!running) {
            lastNanos = System.nanoTime();
            running = true;
        }
        return this;
    }

    public NChronometerBoot stop() {
        if (running) {
            endClock = NClockBoot.now();
            accumulatedNanos += endClock.getTimeNanos() - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NClockBoot getStartClock() {
        return startClock;
    }

    public NClockBoot getEndClock() {
        return endClock;
    }

    public NDurationBoot getDuration() {
        return NDurationBoot.ofNanos(getDurationNanos(), getSmallestUnit(), getLargestUnit());
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

    public NChronometerBoot setSmallestUnit(ChronoUnit smallestUnit) {
        this.smallestUnit = smallestUnit;
        return this;
    }

    public ChronoUnit getLargestUnit() {
        return largestUnit;
    }

    public NChronometerBoot setLargestUnit(ChronoUnit largestUnit) {
        this.largestUnit = largestUnit;
        return this;
    }

}
