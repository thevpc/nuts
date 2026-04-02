package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextFormattable;

/**
 * @author Taha BEN SALAH (taha.bensalah@gmail.com)
 * %creationtime 13 juil. 2006 22:14:21
 */
public class NMemoryMeter implements NTextFormattable {

    private NMemorySnapshot startMemory;
    private NMemorySnapshot endMemory;
    private String name;

    public static NMemoryMeter of() {
        return new NMemoryMeter().start();
    }

    public static NMemoryMeter ofUnstarted() {
        return new NMemoryMeter();
    }

    public NMemoryMeter() {
    }

    public NMemoryMeter copy() {
        NMemoryMeter c = new NMemoryMeter();
        c.name = name;
        c.endMemory = endMemory;
        c.startMemory = startMemory;
        return c;
    }

    /**
     * restart memory meter and returns a stopped snapshot/copy of the current
     *
     * @return
     */
    public NMemoryMeter restart() {
        stop();
        NMemoryMeter c = copy();
        start();
        return c;
    }

    /**
     * restart memory meter with new name and returns a stopped snapshot/copy of the current (with old name)
     *
     * @param name
     * @return
     */
    public NMemoryMeter restart(String name) {
        stop();
        NMemoryMeter c = copy();
        setName(name);
        start();
        return c;
    }

    public NMemoryMeter setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return startMemory != null;
    }

    public boolean isStopped() {
        return endMemory != null;
    }

    public boolean isRunning() {
        return startMemory != null && endMemory == null;
    }

    public NMemoryMeter start() {
        endMemory = null;
        startMemory = NMemorySnapshot.now();
        return this;
    }

    public NMemoryMeter stop() {
        endMemory = NMemorySnapshot.now();
        return this;
    }

    public NMemorySnapshot startSnapshot() {
        return startMemory;
    }

    public NMemorySnapshot endSnapshot() {
        return endMemory;
    }

    public long inUseMemory() {
        return usage().inUseMemory();
    }

    public NMemoryUsage usage() {
        return ((endMemory == null) ? NMemorySnapshot.now() : endMemory).minus(startMemory);
    }


    public String toString() {
        String s = name == null ? "" : name + "=";
        return s + usage().toString();
    }

    @Override
    public NText toText() {
        return usage().toText();
    }
}
