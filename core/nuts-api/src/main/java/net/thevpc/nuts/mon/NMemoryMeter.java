package net.thevpc.nuts.mon;

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

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NMemoryMeter of() {
        return new NMemoryMeter().start();
    }

    /**
     * Creates a new instance of of unstarted.
     *
     * @return of unstarted result
     */
    public static NMemoryMeter ofUnstarted() {
        return new NMemoryMeter();
    }

    /**
     * N memory meter.
     *
     * @return n memory meter result
     */
    public NMemoryMeter() {
    }

    /**
     * Copy.
     *
     * @return copy result
     */
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
      /**
       * Stop.
       */
        stop();
        NMemoryMeter c = copy();
      /**
       * Start.
       */
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
      /**
       * Stop.
       */
        stop();
        NMemoryMeter c = copy();
      /**
       * Sets the name.
       *
       * @param name name
       */
        name(name);
      /**
       * Start.
       */
        start();
        return c;
    }

    /**
     * Sets the name.
     *
     * @param name name
     * @return set name result
     */
    public NMemoryMeter name(String name) {
        this.name = name;
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
        return startMemory != null;
    }

    /**
     * Checks if is stopped.
     *
     * @return is stopped result
     */
    public boolean isStopped() {
        return endMemory != null;
    }

    /**
     * Checks if is running.
     *
     * @return is running result
     */
    public boolean isRunning() {
        return startMemory != null && endMemory == null;
    }

    /**
     * Start.
     *
     * @return start result
     */
    public NMemoryMeter start() {
        endMemory = null;
        startMemory = NMemorySnapshot.now();
        return this;
    }

    /**
     * Stop.
     *
     * @return stop result
     */
    public NMemoryMeter stop() {
        endMemory = NMemorySnapshot.now();
        return this;
    }

    /**
     * Start snapshot.
     *
     * @return start snapshot result
     */
    public NMemorySnapshot startSnapshot() {
        return startMemory;
    }

    /**
     * End snapshot.
     *
     * @return end snapshot result
     */
    public NMemorySnapshot endSnapshot() {
        return endMemory;
    }

    /**
     * In use memory.
     *
     * @return in use memory result
     */
    public long inUseMemory() {
        /**
         * Usage.
         *
         * @param ).inUseMemory( ).in use memory(
         * @return usage result
         */
        return usage().inUseMemory();
    }

    /**
     * Usage.
     *
     * @return usage result
     */
    public NMemoryUsage usage() {
      /**
       * Return.
       *
       * @param endMemory).minus(startMemory end memory).minus(start memory
       */
        return ((endMemory == null) ? NMemorySnapshot.now() : endMemory).minus(startMemory);
    }


    public String toString() {
        String s = name == null ? "" : name + "=";
        return s + usage().toString();
    }

    @Override
    public NText toText() {
        /**
         * Usage.
         *
         * @param ).toText( ).to text(
         * @return usage result
         */
        return usage().toText();
    }
}
