package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NMemorySize;

public class MemoryUtils {
    public static NMemorySize inUseMemory() {
        Runtime rt = Runtime.getRuntime();
        return NMemorySize.ofBytes(rt.totalMemory() - rt.freeMemory());
    }

    public static NMemorySize maxFreeMemory() {
        Runtime rt = Runtime.getRuntime();
        return NMemorySize.ofBytes(rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
    }
}
