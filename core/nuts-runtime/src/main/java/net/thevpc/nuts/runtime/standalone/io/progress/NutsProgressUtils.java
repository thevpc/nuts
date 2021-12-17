package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringMapParser;

import java.util.Map;
import java.util.logging.Level;

public class NutsProgressUtils {
    public static ProgressOptions parseProgressOptions(NutsSession session) {
        ProgressOptions o = new ProgressOptions();
        boolean enabledVisited = false;
        StringMapParser p = new StringMapParser("=", ",; ");
        Map<String, String> m = p.parseMap(session.getProgressOptions(), session);
        NutsElements elems = NutsElements.of(session);
        for (Map.Entry<String, String> e : m.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (!enabledVisited) {
                if (v == null) {
                    Boolean a = NutsUtilStrings.parseBoolean(k, null, null);
                    if (a != null) {
                        o.setEnabled(a);
                        enabledVisited = true;
                    } else {
                        o.put(k, elems.ofString(v));
                    }
                }
            } else {
                o.put(k, elems.ofString(v));
            }
        }
        return o;
    }

    public static boolean acceptProgress(NutsSession session) {
        if (!session.isPlainOut()) {
            return false;
        }
        return !session.isBot() && parseProgressOptions(session).isEnabled();
    }

    public static boolean acceptMonitoring(NutsSession session) {
        // DefaultNutsStreamProgressMonitor is enabled only if plain output
        // so it is disable in json, xml, table, ...
        if (!session.isPlainOut()) {
            return false;
        }
        if (!acceptProgress(session)) {
            return false;
        }
        Object o = session.getProperty("monitor-allowed");
        NutsWorkspace ws = session.getWorkspace();
        if (o != null) {
            o = NutsUtilStrings.parseBoolean(String.valueOf(o),null,null);
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (!session.boot().getBootCustomBoolArgument(false, false, false, "---monitor")) {
            monitorable = false;
        }
        if (ws instanceof DefaultNutsWorkspace) {
            if (!((DefaultNutsWorkspace) ws).LOG.isLoggable(Level.INFO)) {
                monitorable = false;
            }
        }
        return monitorable;
    }
}
