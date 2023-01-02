package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NValue;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProgressOptions {
    public static ProgressOptions of(NSession session) {
        return session.getOrComputeRefProperty(ProgressOptions.class.getName(), s -> {
            ProgressOptions o = new ProgressOptions();
            boolean enabledVisited = false;
            Map<String, String> m = NStringUtils.parseMap(session.getProgressOptions(), "=", ",; ", "").get(session);
            for (Map.Entry<String, String> e : m.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (!enabledVisited) {
                    if (v == null) {
                        Boolean a = NValue.of(k).asBoolean().orNull();
                        if (a != null) {
                            o.setEnabled(a);
                            enabledVisited = true;
                        } else {
                            o.put(k, NValue.of(v));
                        }
                    }else{
                        o.put(k, NValue.of(v));
                    }
                } else {
                    o.put(k, NValue.of(v));
                }
            }
            for (Map.Entry<String, String> e : NConfigs.of(session).getConfigMap().entrySet()) {
                if(e.getKey().startsWith("progress.")){
                    String k = e.getKey().substring("progress.".length());
                    if(o.get(k).isNotPresent()){
                        o.put(k, NValue.of(e.getValue()));
                    }
                }
            }
            return o;
        });
    }

    private final Map<String, NValue> vals = new LinkedHashMap<>();
    private boolean enabled = true;
    private NRef<Level> cachedLevel= null;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isArmedNewline() {
        NOptional<NValue> item = get("newline");
        if(item.isEmpty()){
            item = get("%n");
        }
        if(item.isEmpty()){
            return false;
        }
        return item.flatMap(NValue::asBoolean).orElse(false);
    }
    public Level getArmedLogLevel() {
        if(cachedLevel==null){
            cachedLevel=new NRef<>(
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
                NOptional<NValue> item = get(id);
                if(!item.isEmpty()) {
                    if(item.flatMap(NValue::asBoolean).orElse(true)){
                        return level;
                    }
                    return null;
                }
            }
        }
        NOptional<NValue> item = get("log");
        if(item.isEmpty()){
            return null;
        }
        NValue iValue = item.get();
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

    public ProgressOptions put(String k, NValue e) {
        invalidateCache();
        vals.put(k, e);
        return this;
    }

    private void invalidateCache() {
        this.cachedLevel=null;
    }

    public NOptional<NValue> get(String k) {
        NValue s = vals.get(k);
        return s == null ? NOptional.ofNamedEmpty("property " + k) : NOptional.of(s);
    }


    public ProgressOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        invalidateCache();
        return this;
    }
}
