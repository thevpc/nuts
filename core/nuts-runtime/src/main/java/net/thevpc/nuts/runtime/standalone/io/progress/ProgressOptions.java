package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProgressOptions {
    public static NStringMapFormat COMMAS_FORMAT = new NStringMapFormat("=", ",; ", "", true);

    public static ProgressOptions of(NSession session) {
        return session.getOrComputeRefProperty(ProgressOptions.class.getName(), s -> {
            ProgressOptions o = new ProgressOptions();
            boolean enabledVisited = false;
            Map<String, String> m = COMMAS_FORMAT.parse(session.getProgressOptions()).get(session);
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
            for (Map.Entry<String, String> e : NConfigs.of(session).getConfigMap().entrySet()) {
                if (e.getKey().startsWith("progress.")) {
                    String k = e.getKey().substring("progress.".length());
                    if (o.get(k).isNotPresent()) {
                        o.put(k, NLiteral.of(e.getValue()));
                    }
                }
            }
            return o;
        });
    }

    private final Map<String, NLiteral> vals = new LinkedHashMap<>();
    private boolean enabled = true;
    private NRef<Level> cachedLevel = null;

    public boolean isEnabled() {
        return enabled;
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
