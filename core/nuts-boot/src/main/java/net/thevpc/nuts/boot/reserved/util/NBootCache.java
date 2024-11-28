package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NIdBoot;
import net.thevpc.nuts.boot.reserved.NIdCache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NBootCache {
    public Map<String, Object> cache = new LinkedHashMap<>();
    public Map<NIdBoot, NIdCache> fallbackIdMap = new HashMap<>();
}
