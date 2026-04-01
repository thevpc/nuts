package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextFormattable;

/**
 * @author Taha BEN SALAH (taha.bensalah@gmail.com)
 * %creationtime 13 juil. 2006 22:14:21
 */
public class NMemoryMeter implements NTextFormattable {

    private NMemoryUsage startMemory;
    private NMemoryUsage endMemory;
    private String name;

    public static NMemoryMeter startNow() {
        return new NMemoryMeter().start();
    }

    public NMemoryMeter() {
        start();
    }

    public NMemoryMeter(boolean start) {
        if (start) {
            start();
        }
    }

    public NMemoryMeter copy() {
        NMemoryMeter c = new NMemoryMeter();
        c.name = name;
        c.endMemory = endMemory;
        c.startMemory = startMemory;
        return c;
    }

    /**
     * restart chronometer and returns a stopped snapshot/copy of the current
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
     * restart chronometer with new name and returns a stopped snapshot/copy of the current (with old name)
     *
     * @param newName
     * @return
     */
    public NMemoryMeter restart(String newName) {
        stop();
        NMemoryMeter c = copy();
        setName(newName);
        start();
        return c;
    }

    public NMemoryMeter(String name) {
        this.name = name;
    }

    public NMemoryMeter setName(String desc) {
        this.name = desc;
        return this;
    }

    public NMemoryMeter updateDescription(String desc) {
        setName(desc);
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return startMemory != null && endMemory == null;
    }

    public boolean isStopped() {
        return endMemory == null;
    }

    public NMemoryMeter start() {
        endMemory = null;
        startMemory = new NMemoryUsage();
        return this;
    }

    public NMemoryMeter stop() {
        endMemory = new NMemoryUsage();
        return this;
    }

    public NMemoryUsage getStartMemory() {
        return startMemory;
    }

    public NMemoryUsage getEndMemory() {
        return endMemory;
    }

    public long inUseMemory() {
        return usage().inUseMemory();
    }

    public NMemoryUsage usage() {
        return ((endMemory == null) ? new NMemoryUsage() : endMemory).diff(startMemory);
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
