package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.util.NLiteral;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OsProbeInfoCache {
    static OsProbeInfoCache of(){
        NWorkspace ws = NWorkspace.of();
        synchronized (ws) {
            NLiteral u = ws.getProperty(OsProbeInfoCache.class.getName()).orNull();
            if(u!=null &&  !u.isNull()){
                Object o = u.asObject().orNull();
                if(o instanceof OsProbeInfoCache){
                    return (OsProbeInfoCache) o;
                }
            }
            OsProbeInfoCache r=new OsProbeInfoCache();
            ws.setProperty(OsProbeInfoCache.class.getName(),r);
            return r;
        }
    }

    private Map<String, OsProbeInfo> cache = new ConcurrentHashMap<>();

    public OsProbeInfo get(String id) {
        return cache.computeIfAbsent(id, s -> new OsProbeInfoImpl(id, null));
    }
}
