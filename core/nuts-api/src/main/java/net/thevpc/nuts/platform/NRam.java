package net.thevpc.nuts.platform;

import java.util.Objects;

/**
 * NRam class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NRam {
    private final String name;
    private final long total;
    private final long used;
    private final long free;

    /**
     * N ram.
     *
     * @param name name
     * @param total total
     * @param used used
     * @param free free
     * @return n ram result
     */
    public NRam(String name, long total, long used, long free) {
        this.name = name;
        this.total = total;
        this.used = used;
        this.free = free;
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
     * Converts to al.
     *
     * @return total result
     */
    public long total() {
        return total;
    }

    /**
     * Used.
     *
     * @return used result
     */
    public long used() {
        return used;
    }

    /**
     * Free.
     *
     * @return free result
     */
    public long free() {
        return free;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRam nRam = (NRam) o;
        return total == nRam.total && used == nRam.used && free == nRam.free && Objects.equals(name, nRam.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, total, used, free);
    }
}
