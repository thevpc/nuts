package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.artifact.NId;

import java.util.LinkedHashMap;
import java.util.Map;

public class InstallCache {

    Map<String, InstallIdCacheItem> cached = new LinkedHashMap<>();


    public InstallIdCacheItem get(NId id) {
        return cached.computeIfAbsent(InstallIdCacheItem.normalizeId(id).toString(), e-> new InstallIdCacheItem(id));

    }
}
