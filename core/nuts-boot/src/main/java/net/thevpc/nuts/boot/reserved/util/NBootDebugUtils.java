package net.thevpc.nuts.boot.reserved.util;

import java.util.Map;

public class NBootDebugUtils {
    public static void dumpThreads(){
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            System.err.println(threadEntry.toString());
        }
    }
}
