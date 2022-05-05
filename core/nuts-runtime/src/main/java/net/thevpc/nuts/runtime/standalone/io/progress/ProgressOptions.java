package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsValue;
import net.thevpc.nuts.elem.NutsPrimitiveElement;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isArmedNewline() {
        return get("newline").orElseUse(
                () -> get("%n")
        ).flatMap(NutsValue::asBoolean).orElse(false);
    }

    public ProgressOptions put(String k, NutsValue e) {
        vals.put(k, e);
        return this;
    }

    public NutsOptional<NutsValue> get(String k) {
        NutsValue s = vals.get(k);
        return s == null ? NutsOptional.ofNamedEmpty("property " + k) : NutsOptional.of(s);
    }


    public ProgressOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
