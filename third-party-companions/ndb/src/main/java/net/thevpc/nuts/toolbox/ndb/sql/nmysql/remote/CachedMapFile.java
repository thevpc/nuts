package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;

import java.util.HashMap;
import java.util.Map;

public class CachedMapFile {
    private final NApplicationContext context;
    private Map<String, String> map;
    private boolean enabled;
    private final NPath path;
    private boolean loaded;

    public CachedMapFile(NApplicationContext context, String name) {
        this(context, name, true);
    }

    public CachedMapFile(NApplicationContext context, String name, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
        NId appId = context.getAppId();
        path = context.getTempFolder()
                .resolve(appId.getGroupId() + "-" + appId.getArtifactId() + "-" + appId.getVersion())
                .resolve(name + ".json");
        if (enabled) {
            if (path.isRegularFile()) {
                try {
                    NSession session = context.getSession();
                    map = NElements.of(session)
                            .json()
                            .parse(path, Map.class);
                    loaded = true;
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    public boolean exists() {
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CachedMapFile setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean contains(String k) {
        if (!enabled) {
            return false;
        }
        if (map == null) {
            return false;
        }
        return map.get(k) != null;
    }

    public String get(String k) {
        if (!enabled) {
            return null;
        }
        if (map == null) {
            return null;
        }
        return map.get(k);
    }

    public boolean is(String k) {
        return "true".equals(get(k));
    }

    public void put(String k) {
        put("k", "true");
    }

    public void put(String k, String v) {
        if (enabled) {
            if (map == null) {
                map = new HashMap<String, String>();
            }
            map.put(k, v);
            try {
                NSession session = context.getSession();
                NElements.of(session).setValue(map)
                        .json()
                        .setNtf(false)
                        .print(path);
            } catch (Exception ex) {
                //
            }
        }
    }

    public void reset() {
        dispose();
        map = null;
    }

    public void dispose() {
        if (path != null) {
            path.delete();
        }
    }
}
