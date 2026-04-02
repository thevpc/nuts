package net.thevpc.nuts.util;

import net.thevpc.nuts.text.*;

import java.util.Objects;

/**
 * Created by vpc on 3/24/17.
 */
public final class NMemorySnapshot implements NTextFormattable {
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;

    public static NMemorySnapshot now() {
        Runtime rt = Runtime.getRuntime();
        return new NMemorySnapshot(rt.maxMemory(), rt.totalMemory(), rt.freeMemory());
    }

    public NMemorySnapshot(long maxMemory, long totalMemory, long freeMemory) {
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
    }

    public NMemoryUsage minus(NMemorySnapshot other) {
        if (other == null) {
            return new NMemoryUsage(
                    totalMemory,
                    freeMemory
            );
        }
        return new NMemoryUsage(
                totalMemory - other.totalMemory,
                freeMemory - other.freeMemory
        );
    }


    public long maxMemory() {
        return maxMemory;
    }

    public long totalMemory() {
        return totalMemory;
    }

    public long freeMemory() {
        return freeMemory;
    }

    public long inUseMemory() {
        return totalMemory - freeMemory;
    }

    @Override
    public NText toText() {
        NTextFormat<Number> p = NTextFormat.ofBytes(null);
        return NTextBuilder.of()
                .append("free : ")
                .append(p.toText(freeMemory()))
                .append(" ; ")
                .append("total : ")
                .append(p.toText(totalMemory()))
                .append(" ; ")
                .append("max : ")
                .append(p.toText(maxMemory()))
                .append(" ; ")
                .append("inUse : ")
                .append(p.toText(inUseMemory()))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NMemorySnapshot that = (NMemorySnapshot) o;
        return maxMemory == that.maxMemory && totalMemory == that.totalMemory && freeMemory == that.freeMemory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxMemory, totalMemory, freeMemory);
    }

    public String toString() {
        return
                "free : " + freeMemory() + " ; "
                        + "total : " + totalMemory() + " ; "
                        + "max : " + maxMemory() + " ; "
                        + "inUse : " + inUseMemory()
                ;
    }
}
