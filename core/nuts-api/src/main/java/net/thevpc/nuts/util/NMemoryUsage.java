package net.thevpc.nuts.util;

import net.thevpc.nuts.text.*;

import java.util.Objects;

/**
 * Created by vpc on 3/24/17.
 */
public final class NMemoryUsage implements NTextFormattable {
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;

    public static NMemoryUsage of() {
        return new NMemoryUsage();
    }

    public NMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        maxMemory = rt.maxMemory();
        totalMemory = rt.totalMemory();
        freeMemory = rt.freeMemory();
    }

    public NMemoryUsage(long maxMemory, long totalMemory, long freeMemory) {
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
    }

    public NMemoryUsage diff(NMemoryUsage other) {
        if (other == null) {
            return this;
        }
        return new NMemoryUsage(
                maxMemory,
                totalMemory,
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
        NTextFormat<Number> p = NTextFormat.ofBytes( null);
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
                .append(" ; ")
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NMemoryUsage that = (NMemoryUsage) o;
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
