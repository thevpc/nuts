package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.net.NConnexionString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OsProbeInfoCache {
    static OsProbeInfoCache of(){
        NWorkspace ws = NWorkspace.of();
        synchronized (ws) {
            OsProbeInfoCache u = ws.getProperty(OsProbeInfoCache.class).orNull();
            if(u!=null){
                return u;
            }
            OsProbeInfoCache r=new OsProbeInfoCache();
            ws.setProperty(OsProbeInfoCache.class.getName(),r);
            return r;
        }
    }

    private Map<NConnexionString, OsProbeInfo> cache = new ConcurrentHashMap<>();

    public OsProbeInfo get(NConnexionString id) {
        return cache.computeIfAbsent(id, s -> new OsProbeInfoImpl(id));
    }

    public OsProbeInfo get(String id) {
        return get(NConnexionString.of(id));
    }
}
