package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootId;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NBootCache {
    final private Map<String, Object> cache = new LinkedHashMap<>();
    public Map<NBootId, NBootIdCache> fallbackIdMap = new HashMap<>();

    public Object get(String key, Function<String, Object> mappingFunction) {
        //cannot use cache.computeIfAbsent because of reentrant calls!
        synchronized (cache) {
            Object o = cache.get(key);
            if (o != null) {
                return o;
            }
            o = mappingFunction.apply(key);
            cache.put(key, o);
            return o;
        }
    }
}
