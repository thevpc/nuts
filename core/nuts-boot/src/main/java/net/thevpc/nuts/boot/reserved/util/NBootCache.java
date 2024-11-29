package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootId;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NBootCache {
    public Map<String, Object> cache = new LinkedHashMap<>();
    public Map<NBootId, NBootIdCache> fallbackIdMap = new HashMap<>();
}
