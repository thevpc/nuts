package net.thevpc.nuts.boot;

import net.thevpc.nuts.NId;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NBootCache {
    public Map<String, Object> cache = new LinkedHashMap<>();
    public Map<NId, NIdCache> fallbackIdMap = new HashMap<>();
}
