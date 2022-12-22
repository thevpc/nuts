/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsFormattable;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsMapBy;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.text.NutsTextStyle;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author taha.bensalah@gmail.com
 */
public class NutsChronometer implements Serializable, NutsFormattable {

    private final static long serialVersionUID = 1L;
    private long accumulatedNanos;
    private NutsClock startClock;
    private NutsClock endClock;
    private String name;
    private long lastNanos;
    private boolean running;
    private ChronoUnit smallestUnit;
    private ChronoUnit largestUnit;

    public static NutsChronometer startNow() {
        return startNow(null, null);
    }

    public static NutsChronometer startNow(String name) {
        return startNow(name, null);
    }

    public static NutsChronometer startNow(ChronoUnit smallestUnit) {
        return startNow(null, smallestUnit);
    }

    public static NutsChronometer startNow(String name, ChronoUnit smallestUnit) {
        return new NutsChronometer(name, smallestUnit).start();
    }

    public NutsChronometer() {
    }

    public NutsChronometer copy() {
        return new NutsChronometer(
                name, startClock, endClock, accumulatedNanos, lastNanos, running, smallestUnit, largestUnit
        );
    }

    @NutsMapBy
    public NutsChronometer(
            @NutsMapBy(name = "name") String name,
            @NutsMapBy(name = "startClock") NutsClock startClock,
            @NutsMapBy(name = "endClock") NutsClock endClock,
            @NutsMapBy(name = "accumulatedNanos") long accumulatedNanos,
            @NutsMapBy(name = "lastNanos") long lastNanos,
            @NutsMapBy(name = "running") boolean running,
            @NutsMapBy(name = "smallestUnit") ChronoUnit smallestUnit,
            @NutsMapBy(name = "largestUnit") ChronoUnit largestUnit) {
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
     * @return
     */
    public NutsChronometer restart() {
        stop();
        NutsChronometer c = copy();
        start();
        return c;
    }

    /**
     * restart chronometer with new name and returns a stopped snapshot/copy of
     * the current (with old name)
     *
     * @param newName
     * @return
     */
    public NutsChronometer restart(String newName) {
        stop();
        NutsChronometer c = copy();
        setName(newName);
        start();
        return c;
    }

    public NutsChronometer(String name) {
        this.name = name;
    }

    public NutsChronometer(String name, ChronoUnit smallestUnit) {
        this.name = name;
        this.smallestUnit = smallestUnit;
    }

    public NutsChronometer setName(String desc) {
        this.name = desc;
        return this;
    }

    public NutsChronometer updateDescription(String desc) {
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
        return endClock!=null;
    }

    public NutsChronometer reset() {
        endClock = null;
        startClock = null;
        lastNanos = 0;
        accumulatedNanos = 0;
        running = false;
        return this;
    }

    public NutsChronometer start() {
        endClock = null;
        startClock = NutsClock.now();
        lastNanos = startClock.getNanos();
        accumulatedNanos = 0;
        running = true;
        return this;
    }

    public NutsChronometer accumulate() {
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

    public NutsChronometer suspend() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NutsChronometer resume() {
        if (!running) {
            lastNanos = System.nanoTime();
            running = true;
        }
        return this;
    }

    public NutsChronometer stop() {
        if (running) {
            endClock = NutsClock.now();
            accumulatedNanos += endClock.getNanos() - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NutsClock getStartClock() {
        return startClock;
    }

    public NutsClock getEndClock() {
        return endClock;
    }

    public NutsDuration getDuration() {
        return NutsDuration.ofNanos(getDurationNanos(), getSmallestUnit(), getLargestUnit());
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
        return toString(null);
    }

    public String toString(NutsDurationFormatMode mode) {
        String s = name == null ? "" : name + "=";
        return s + getDuration().toString(mode);
    }


    public ChronoUnit getSmallestUnit() {
        return smallestUnit;
    }

    public NutsChronometer setSmallestUnit(ChronoUnit smallestUnit) {
        this.smallestUnit = smallestUnit;
        return this;
    }

    public ChronoUnit getLargestUnit() {
        return largestUnit;
    }

    public NutsChronometer setLargestUnit(ChronoUnit largestUnit) {
        this.largestUnit = largestUnit;
        return this;
    }


    @Override
    public NutsFormat formatter(NutsSession session) {
        return NutsFormat.of(session, new NutsFormatSPI() {
            private NutsDurationFormatMode formatMode;

            @Override
            public String getName() {
                return "chronometer";
            }

            @Override
            public void print(NutsPrintStream out) {
                if (name != null) {
                    out.append(name);
                    out.append("=", NutsTextStyle.separator());
                }
                out.print(getDuration().formatter(session)
                        .configure(true,
                                "--mode",
                                (formatMode == null ? NutsDurationFormatMode.DEFAULT : formatMode).id())
                        .format());
            }

            @Override
            public boolean configureFirst(NutsCommandLine commandLine) {
                NutsArgument a = commandLine.peek().get(session);
                switch (a.key()) {
                    case "--mode": {
                        a = commandLine.nextString().get(session);
                        if (a.isActive()) {
                            formatMode = NutsDurationFormatMode.parse(a.getStringValue().get()).get();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
