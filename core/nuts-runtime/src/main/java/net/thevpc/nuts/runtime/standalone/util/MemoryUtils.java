package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NutsMemorySize;

public class MemoryUtils {
    public static NutsMemorySize inUseMemory() {
        Runtime rt = Runtime.getRuntime();
        return NutsMemorySize.ofBytes(rt.totalMemory() - rt.freeMemory());
    }

    public static NutsMemorySize maxFreeMemory() {
        Runtime rt = Runtime.getRuntime();
        return NutsMemorySize.ofBytes(rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
    }
}
