/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.time;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.NFormattable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author taha.bensalah@gmail.com
 */
public class NChronometer implements Serializable, NFormattable {

    private final static long serialVersionUID = 1L;
    private long accumulatedNanos;
    private NClock startClock;
    private NClock endClock;
    private String name;
    private long lastNanos;
    private boolean running;
    private ChronoUnit smallestUnit;
    private ChronoUnit largestUnit;

    public static NChronometer startNow() {
        return startNow(null, null);
    }

    public static NChronometer startNow(String name) {
        return startNow(name, null);
    }

    public static NChronometer startNow(ChronoUnit smallestUnit) {
        return startNow(null, smallestUnit);
    }

    public static NChronometer startNow(String name, ChronoUnit smallestUnit) {
        return new NChronometer(name, smallestUnit).start();
    }

    public NChronometer() {
    }

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
        stop();
        NChronometer c = copy();
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
        stop();
        NChronometer c = copy();
        setName(newName);
        start();
        return c;
    }

    public NChronometer(String name) {
        this.name = name;
    }

    public NChronometer(String name, ChronoUnit smallestUnit) {
        this.name = name;
        this.smallestUnit = smallestUnit;
    }

    public NChronometer setName(String desc) {
        this.name = desc;
        return this;
    }

    public NChronometer updateDescription(String desc) {
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

    public NChronometer reset() {
        endClock = null;
        startClock = null;
        lastNanos = 0;
        accumulatedNanos = 0;
        running = false;
        return this;
    }

    public NChronometer start() {
        endClock = null;
        startClock = NClock.now();
        lastNanos = startClock.getTimeNanos();
        accumulatedNanos = 0;
        running = true;
        return this;
    }

    public NChronometer accumulate() {
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

    public NChronometer suspend() {
        if (running) {
            long n = System.nanoTime();
            accumulatedNanos += n - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NChronometer resume() {
        if (!running) {
            lastNanos = System.nanoTime();
            running = true;
        }
        return this;
    }

    public NChronometer stop() {
        if (running) {
            endClock = NClock.now();
            accumulatedNanos += endClock.getTimeNanos() - lastNanos;
            lastNanos = -1;
            running = false;
        }
        return this;
    }

    public NClock getStartClock() {
        return startClock;
    }

    public NClock getEndClock() {
        return endClock;
    }

    public NDuration getDuration() {
        return NDuration.ofNanos(getDurationNanos(), getSmallestUnit(), getLargestUnit());
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

    public String toString(NDurationFormatMode mode) {
        String s = name == null ? "" : name + "=";
        return s + getDuration().toString(mode);
    }


    public ChronoUnit getSmallestUnit() {
        return smallestUnit;
    }

    public NChronometer setSmallestUnit(ChronoUnit smallestUnit) {
        this.smallestUnit = smallestUnit;
        return this;
    }

    public ChronoUnit getLargestUnit() {
        return largestUnit;
    }

    public NChronometer setLargestUnit(ChronoUnit largestUnit) {
        this.largestUnit = largestUnit;
        return this;
    }


    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NFormatSPI() {
            private NDurationFormatMode formatMode;

            @Override
            public String getName() {
                return "chronometer";
            }

            @Override
            public void print(NPrintStream out) {
                if (name != null) {
                    out.print(name);
                    out.print("=", NTextStyle.separator());
                }
                out.print(getDuration().formatter(session)
                        .configure(true,
                                "--mode",
                                (formatMode == null ? NDurationFormatMode.DEFAULT : formatMode).id())
                        .format());
            }

            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                NArg a = cmdLine.peek().get(session);
                switch (a.key()) {
                    case "--mode": {
                        a = cmdLine.nextEntry().get(session);
                        if (a.isActive()) {
                            formatMode = NDurationFormatMode.parse(a.getStringValue().get()).get();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
