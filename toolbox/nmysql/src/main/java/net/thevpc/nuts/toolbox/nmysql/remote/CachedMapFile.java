package net.thevpc.nuts.toolbox.nmysql.remote;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CachedMapFile {
    private NutsApplicationContext context;
    private Map<String, String> map;
    private boolean enabled;
    private Path path;
    private boolean loaded;

    public CachedMapFile(NutsApplicationContext context, String name) {
        this(context,name,true);
    }

    public CachedMapFile(NutsApplicationContext context, String name, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
        NutsId appId = context.getAppId();
        path = Paths.get(context.getTempFolder())
                .resolve(appId.getGroupId() + "-" + appId.getArtifactId() + "-" + appId.getVersion())
                .resolve(name + ".json");
        if (enabled) {
            if (Files.isRegularFile(path)) {
                try {
                    map = context.getWorkspace().formats().element()
                            .setContentType(NutsContentType.JSON)
                            .parse(path, Map.class);
                    loaded=true;
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    public CachedMapFile setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean exists(){
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean contains(String k) {
        if (!enabled) {
            return false;
        }
        if (map == null) {
            return false;
        }
        return map.get(k)!=null;
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
                context.getWorkspace().formats().element().setValue(map)
                        .setContentType(NutsContentType.JSON)
                        .print(path);
            } catch (Exception ex) {
                //
            }
        }
    }

    public void reset() {
        dispose();
        map=null;
    }

    public void dispose() {
        if (path != null) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                //
            }
        }
    }
}
