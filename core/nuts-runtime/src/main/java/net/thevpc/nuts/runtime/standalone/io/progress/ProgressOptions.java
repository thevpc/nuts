package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsValue;
import net.thevpc.nuts.elem.NutsPrimitiveElement;
import net.thevpc.nuts.util.NutsRef;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProgressOptions {
    public static ProgressOptions of(NutsSession session) {
        return session.getOrComputeRefProperty(ProgressOptions.class.getName(), s -> {
            ProgressOptions o = new ProgressOptions();
            boolean enabledVisited = false;
            Map<String, String> m = NutsStringUtils.parseMap(session.getProgressOptions(), "=", ",; ", "").get(session);
            for (Map.Entry<String, String> e : m.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (!enabledVisited) {
                    if (v == null) {
                        Boolean a = NutsValue.of(k).asBoolean().orNull();
                        if (a != null) {
                            o.setEnabled(a);
                            enabledVisited = true;
                        } else {
                            o.put(k, NutsValue.of(v));
                        }
                    }else{
                        o.put(k, NutsValue.of(v));
                    }
                } else {
                    o.put(k, NutsValue.of(v));
                }
            }
            for (Map.Entry<String, String> e : session.config().getConfigMap().entrySet()) {
                if(e.getKey().startsWith("progress.")){
                    String k = e.getKey().substring("progress.".length());
                    if(o.get(k).isNotPresent()){
                        o.put(k,NutsValue.of(e.getValue()));
                    }
                }
            }
            return o;
        });
    }

    private final Map<String, NutsValue> vals = new LinkedHashMap<>();
    private boolean enabled = true;
    private NutsRef<Level> cachedLevel= null;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isArmedNewline() {
        NutsOptional<NutsValue> item = get("newline");
        if(item.isEmpty()){
            item = get("%n");
        }
        if(item.isEmpty()){
            return false;
        }
        return item.flatMap(NutsValue::asBoolean).orElse(false);
    }
    public Level getArmedLogLevel() {
        if(cachedLevel==null){
            cachedLevel=new NutsRef<>(
                    getArmedLogLevel0()
            );
        }
        return cachedLevel.get();
    }

    private Level getArmedLogLevel0() {
        for (Level level : new Level[]{Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST,Level.ALL}) {
            String[] ids=new String[]{"log-"+level.getName().toLowerCase()};
            if(level==Level.FINEST){
                ids=new String[]{"log-"+level.getName().toLowerCase(),"log-verbose"};
            }
            for (String id : ids) {
                NutsOptional<NutsValue> item = get(id);
                if(!item.isEmpty()) {
                    if(item.flatMap(NutsValue::asBoolean).orElse(true)){
                        return level;
                    }
                    return null;
                }
            }
        }
        NutsOptional<NutsValue> item = get("log");
        if(item.isEmpty()){
            return null;
        }
        NutsValue iValue = item.get();
        String s = iValue.asString().orNull();
        if(s!=null){
            for (Level level : new Level[]{Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST,Level.ALL}) {
                String[] ids=new String[]{"log-"+level.getName().toLowerCase()};
                if(level==Level.FINEST){
                    ids=new String[]{level.getName().toLowerCase(),"verbose"};
                }
                for (String id : ids) {
                    if(id.equals(s.toLowerCase())){
                            return level;
                    }
                }
            }
        }
        return iValue.asBoolean().orElse(true)?Level.FINEST : null;
    }

    public ProgressOptions put(String k, NutsValue e) {
        invalidateCache();
        vals.put(k, e);
        return this;
    }

    private void invalidateCache() {
        this.cachedLevel=null;
    }

    public NutsOptional<NutsValue> get(String k) {
        NutsValue s = vals.get(k);
        return s == null ? NutsOptional.ofNamedEmpty("property " + k) : NutsOptional.of(s);
    }


    public ProgressOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        invalidateCache();
        return this;
    }
}
