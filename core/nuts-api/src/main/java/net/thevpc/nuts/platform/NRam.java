package net.thevpc.nuts.platform;

import java.util.Objects;

public class NRam {
    private final String name;
    private final long total;
    private final long used;
    private final long free;

    public NRam(String name, long total, long used, long free) {
        this.name = name;
        this.total = total;
        this.used = used;
        this.free = free;
    }

    public String name() {
        return name;
    }

    public long total() {
        return total;
    }

    public long used() {
        return used;
    }

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
