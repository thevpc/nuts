package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.elem.NutsPrimitiveElement;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProgressOptions {
    private final Map<String, NutsPrimitiveElement> vals = new LinkedHashMap<>();
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isArmedNewline() {
        return isArmed("newline") || isArmed("%n");
    }

    public ProgressOptions put(String k,NutsPrimitiveElement e) {
        vals.put(k,e);
        return this;
    }
    public NutsPrimitiveElement get(String k) {
        return vals.get(k);
    }
    public boolean isArmed(String k) {
        NutsPrimitiveElement q = get(k);
        if (q == null) {
            return false;
        }
        return q.asBoolean().orElse(true);
    }

    public ProgressOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
