package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.*;

import net.thevpc.nuts.elem.NStringElement;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProgressOptions {

    public static ProgressOptions of() {
        return NApp.of().getOrComputeProperty(ProgressOptions.class.getName(), NScopeType.SESSION, () -> {
            ProgressOptions o = new ProgressOptions();
            boolean enabledVisited = false;
            Map<String, String> m = NStringMapFormat.COMMA_FORMAT.parse(NSession.of().getProgressOptions()).get();
            for (Map.Entry<String, String> e : m.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (!enabledVisited) {
                    if (v == null) {
                        Boolean a = NLiteral.of(k).asBoolean().orNull();
                        if (a != null) {
                            o.setEnabled(a);
                            enabledVisited = true;
                        } else {
                            o.put(k, NLiteral.of(v));
                        }
                    } else {
                        o.put(k, NLiteral.of(v));
                    }
                } else {
                    o.put(k, NLiteral.of(v));
                }
            }
            for (Map.Entry<String, String> e : NWorkspace.of().getConfigMap().entrySet()) {
                if (e.getKey().startsWith("progress.")) {
                    String k = e.getKey().substring("progress.".length());
                    if (o.get(k).isNotPresent()) {
                        o.put(k, NLiteral.of(e.getValue()));
                    }
                }
            }
            if (o.getEnabled().isEmpty() && !o.vals.isEmpty()) {
                // if we specify progress options,
                // this means we forcibly want progress to be shon
                o.setEnabled(true);
            }
            return o;
        });
    }

    private final Map<String, NLiteral> vals = new LinkedHashMap<>();
    private Boolean enabled;
    private NRef<Level> cachedLevel = null;

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public NOptional<Boolean> getEnabled() {
        return NOptional.of(enabled);
    }

    public boolean isArmedNewline() {
        NOptional<NLiteral> item = get("newline");
        if (item.isEmpty()) {
            item = get("%n");
        }
        if (item.isEmpty()) {
            return false;
        }
        return item.isBlank() || item.flatMap(NLiteral::asBoolean).orElse(false);
    }

    public Level getArmedLogLevel() {
        if (cachedLevel == null) {
            cachedLevel = new NRef<>(
                    getArmedLogLevel0()
            );
        }
        return cachedLevel.get();
    }

    private Level getArmedLogLevel0() {
        for (Level level : new Level[]{Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL}) {
            String[] ids = new String[]{"log-" + level.getName().toLowerCase()};
            if (level == Level.FINEST) {
                ids = new String[]{"log-" + level.getName().toLowerCase(), "log-verbose"};
            }
            for (String id : ids) {
                NOptional<NLiteral> item = get(id);
                if (!item.isEmpty()) {
                    if (item.flatMap(NLiteral::asBoolean).orElse(true)) {
                        return level;
                    }
                    return null;
                }
            }
        }
        NOptional<NLiteral> item = get("log");
        if (item.isEmpty()) {
            return null;
        }
        NLiteral iValue = item.get();
        String s = iValue.asString().orNull();
        if (s != null) {
            for (Level level : new Level[]{Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL}) {
                String[] ids = new String[]{"log-" + level.getName().toLowerCase()};
                if (level == Level.FINEST) {
                    ids = new String[]{level.getName().toLowerCase(), "verbose"};
                }
                for (String id : ids) {
                    if (id.equals(s.toLowerCase())) {
                        return level;
                    }
                }
            }
        }
        return iValue.asBoolean().orElse(true) ? Level.FINEST : null;
    }

    public ProgressOptions put(String k, NLiteral e) {
        invalidateCache();
        vals.put(k, e);
        return this;
    }

    private void invalidateCache() {
        this.cachedLevel = null;
    }

    public NOptional<NLiteral> get(String k) {
        NLiteral s = vals.get(k);
        return s == null ? NOptional.ofNamedEmpty("property " + k) : NOptional.of(s);
    }


    public ProgressOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        invalidateCache();
        return this;
    }
}
