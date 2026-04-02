package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextFormat;
import net.thevpc.nuts.text.NTextFormattable;

import java.util.Objects;

/**
 * Created by vpc on 3/24/17.
 */
public final class NMemoryUsage implements NTextFormattable {
    private final long totalMemory;
    private final long freeMemory;
    public static final NMemoryUsage ZERO=new NMemoryUsage(0,0);

    public NMemoryUsage(long totalMemory, long freeMemory) {
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
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
        return totalMemory == that.totalMemory && freeMemory == that.freeMemory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMemory, freeMemory);
    }

    public String toString() {
        return
                "free : " + freeMemory() + " ; "
                        + "total : " + totalMemory() + " ; "
                        + "inUse : " + inUseMemory()
                ;
    }
}
