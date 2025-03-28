package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

public class NWorkspaceProfilerImpl {
    public static final Map<String, AtomicLong> map = new HashMap<>();

    public static AtomicLong of(String s) {
        synchronized (map) {
            return map.computeIfAbsent(s, k -> new AtomicLong());
        }
    }

    public static void sleep(long ms, String name) {
        if (ms > 0) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            of(name).accumulateAndGet(ms, Long::sum);
            of("sleep").accumulateAndGet(ms, Long::sum);
        }
    }

    public static void debug() {
        Set<String> keys;
        synchronized (map) {
            keys = new TreeSet<>(map.keySet());
        }
        NLog nLog = NLog.of(NWorkspaceProfilerImpl.class);
        for (String key : keys) {
            AtomicLong aLong;
            synchronized (map) {
                aLong = map.get(key);
            }
            nLog.debug(NMsg.ofC("NWorkspaceProfiler %s: %s", key, aLong.get()));
        }
    }
}
