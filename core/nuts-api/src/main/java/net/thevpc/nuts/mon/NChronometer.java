/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.mon;

import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * @author taha.bensalah@gmail.com
 */
public class NChronometer implements Serializable {

    private final static long serialVersionUID = 1L;
    private long accumulatedNanos;
    private NClock startClock;
    private NClock endClock;
    private String name;
    private long lastNanos;
    private boolean running;
    private ChronoUnit smallestUnit;
    private ChronoUnit largestUnit;

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NChronometer of() {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param null null
         * @return of result
         */
        return of(null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @return of result
     */
    public static NChronometer of(String name) {
        /**
         * Creates a new instance of of.
         *
         * @param name name
         * @param null null
         * @return of result
         */
        return of(name, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param smallestUnit smallest unit
     * @return of result
     */
    public static NChronometer of(ChronoUnit smallestUnit) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param smallestUnit smallest unit
         * @return of result
         */
        return of(null, smallestUnit);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param smallestUnit smallest unit
     * @return of result
     */
    public static NChronometer of(String name, ChronoUnit smallestUnit) {
        return new NChronometer(name, smallestUnit).start();
    }

    /**
     * Creates a new instance of of unstarted.
     *
     * @return of unstarted result
     */
    public static NChronometer ofUnstarted() {
        /**
         * Creates a new instance of of unstarted.
         *
         * @param null null
         * @param null null
         * @return of unstarted result
         */
        return ofUnstarted(null, null);
    }

    /**
     * Creates a new instance of of unstarted.
     *
     * @param name name
     * @return of unstarted result
     */
    public static NChronometer ofUnstarted(String name) {
        /**
         * Creates a new instance of of unstarted.
         *
         * @param name name
         * @param null null
         * @return of unstarted result
         */
        return ofUnstarted(name, null);
    }

    /**
     * Creates a new instance of of unstarted.
     *
     * @param smallestUnit smallest unit
     * @return of unstarted result
     */
    public static NChronometer ofUnstarted(ChronoUnit smallestUnit) {
        /**
         * Creates a new instance of of unstarted.
         *
         * @param null null
         * @param smallestUnit smallest unit
         * @return of unstarted result
         */
        return ofUnstarted(null, smallestUnit);
    }

    /**
     * Creates a new instance of of unstarted.
     *
     * @param name name
     * @param smallestUnit smallest unit
     * @return of unstarted result
     */
    public static NChronometer ofUnstarted(String name, ChronoUnit smallestUnit) {
        return new NChronometer(name, smallestUnit);
    }

    /**
     * N chronometer.
     *
     * @return n chronometer result
     */
    public NChronometer() {
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NChronometer copy() {
        return new NChronometer(
                name, startClock, endClock, accumulatedNanos, lastNanos, running, smallestUnit, largestUnit
        );
    }

    @NMapBy
    public NChronometer(
            @NMapBy(name = "name") String name,
            @NMapBy(name = "startClock") NClock startClock,
            @NMapBy(name = "endClock") NClock endClock,
            @NMapBy(name = "accumulatedNanos") long accumulatedNanos,
            @NMapBy(name = "lastNanos") long lastNanos,
            @NMapBy(name = "running") boolean running,
            @NMapBy(name = "smallestUnit") ChronoUnit smallestUnit,
            @NMapBy(name = "largestUnit") ChronoUnit largestUnit) {
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
    public NChronometer restart() {
      /**
       * Stop.
       */
        stop();
        NChronometer c = copy();
      /**
       * Start.
       */
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
    public NChronometer restart(String newName) {
      /**
       * Stop.
       */
        stop();
        NChronometer c = copy();
      /**
       * Sets the name.
       *
       * @param newName new name
       */
        setName(newName);
      /**
       * Start.
       */
        start();
        return c;
    }

    /**
     * N chronometer.
     *
     * @param name name
     * @return n chronometer result
     */
    public NChronometer(String name) {
        this.name = name;
    }

    /**
     * N chronometer.
     *
     * @param name name
     * @param smallestUnit smallest unit
     * @return n chronometer result
     */
    public NChronometer(String name, ChronoUnit smallestUnit) {
        this.name = name;
        this.smallestUnit = smallestUnit;
    }

    /**
     * Sets the name.
     *
     * @param desc desc
     * @return set name result
     */
    public NChronometer setName(String desc) {
        this.name = desc;
        return this;
    }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }

    /**
     * Checks if is started.
     *
     * @return is started result
     */
    public boolean isStarted() {
        return startClock != null;
    }

    /**
     * Checks if is stopped.
     *
     * @return is stopped result
     */
    public boolean isStopped() {
        return endClock != null;
    }

    /**
     * Reset.
     *
     * @return reset result
     */
    public NChronometer reset() {
        endClock = null;
        startClock = null;
        lastNanos = 0;
        accumulatedNanos = 0;
        running = false;
        return this;
    }

    /**
     * Start.
     *
     * @return start result
     */
    public NChronometer start() {
        endClock = null;
        startClock = NClock.now();
        lastNanos = startClock.timeNanos();
        accumulatedNanos = 0;
        running = true;
        return this;
    }

    /**
     * Accumulate.
     *
     * @return accumulate result
     */
    public NChronometer accumulate() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = n;
        }
        return this;
    }

    /**
     * Lap.
     *
     * @return lap result
     */
    public NDuration lap() {
        if (running) {
            long n = System.nanoTime();
            long lapValue = n - lastNanos;
            this.accumulatedNanos += lapValue;
            lastNanos = n;
            return NDuration.ofNanos(lapValue);
        }
        return NDuration.ZERO;
    }

    /**
     * Checks if is suspended.
     *
     * @return is suspended result
     */
    public boolean isSuspended() {
        return !running;
    }

    /**
     * Suspend.
     *
     * @return suspend result
     */
    public NChronometer suspend() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    /**
     * Resume.
     *
     * @return resume result
     */
    public NChronometer resume() {
        if (!running) {
            lastNanos = System.nanoTime();
            running = true;
        }
        return this;
    }

    /**
     * Stop.
     *
     * @return stop result
     */
    public NChronometer stop() {
        if (running) {
            endClock = NClock.now();
            accumulatedNanos += endClock.timeNanos() - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    /**
     * Start clock.
     *
     * @return start clock result
     */
    public NClock startClock() {
        return startClock;
    }

    /**
     * End clock.
     *
     * @return end clock result
     */
    public NClock endClock() {
        return endClock;
    }

    /**
     * Duration.
     *
     * @return duration result
     */
    public NDuration duration() {
        return NDuration.ofNanos(durationNanos(), smallestUnit(), largestUnit());
    }

    /**
     * Duration ms.
     *
     * @return duration ms result
     */
    public long durationMs() {
        return durationNanos() / 1000000L;
    }

    /**
     * Duration nanos.
     *
     * @return duration nanos result
     */
    public long durationNanos() {
        if (startClock == null) {
            return 0;
        }
        if (running) {
            long curr = System.nanoTime() - lastNanos;
          /**
           * Return.
           *
           * @param accumulatedNanos accumulated nanos
           */
            return (curr + accumulatedNanos);
        }
        return accumulatedNanos;
    }

    /**
     * Returns string representation of chronometer.
     *
     * @return formatted chronometer string
     */
    @Override
    public String toString() {
        return toString(null);
    }

    /**
     * Returns string representation of chronometer using specified format mode.
     *
     * @param mode duration format mode
     * @return formatted chronometer string
     */
    public String toString(NDurationFormatMode mode) {
        String s = name == null ? "" : name + "=";
        return s + duration().toString(mode);
    }

    /**
     * Smallest unit.
     *
     * @return smallest unit result
     */
    public ChronoUnit smallestUnit() {
        return smallestUnit;
    }

    /**
     * Sets the smallest unit.
     *
     * @param smallestUnit smallest unit
     * @return set smallest unit result
     */
    public NChronometer setSmallestUnit(ChronoUnit smallestUnit) {
        this.smallestUnit = smallestUnit;
        return this;
    }

    /**
     * Largest unit.
     *
     * @return largest unit result
     */
    public ChronoUnit largestUnit() {
        return largestUnit;
    }

    /**
     * Sets the largest unit.
     *
     * @param largestUnit largest unit
     * @return set largest unit result
     */
    public NChronometer setLargestUnit(ChronoUnit largestUnit) {
        this.largestUnit = largestUnit;
        return this;
    }

    /**
     * As read only.
     *
     * @return as read only result
     */
    public NChronometerView asReadOnly() {
        return new NReadOnlyChronometer(this);
    }

}
