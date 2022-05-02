package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsStreamMetadata;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressMonitor;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NutsProgressUtils {
    public static ProgressOptions parseProgressOptions(NutsSession session) {
        ProgressOptions o = new ProgressOptions();
        boolean enabledVisited = false;
        Map<String, String> m = NutsUtilStrings.parseMap(session.getProgressOptions(), "=", ",; ","").get(session);
        NutsElements elems = NutsElements.of(session);
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

    public static java.io.InputStream monitor(URL from, NutsProgressMonitor monitor, NutsSession session) {
        return monitor(
                NutsPath.of(from,session).getInputStream(),
                from, NutsTexts.of(session).ofStyled(
                        NutsPath.of(from,session).getName()
                        , NutsTextStyle.path()),
                NutsPath.of(from, session).getContentLength(), monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsString sourceName, long length, NutsProgressMonitor monitor, NutsSession session) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsProgressMonitor monitor, NutsSession session) {
        NutsString sourceName = null;
        long length = -1;
        NutsStreamMetadata m = NutsStreamMetadata.resolve(from);
        if (m != null) {
            sourceName = NutsTexts.of(session).toText(m.getName());
            length = m.getContentLength();
        }
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static NutsProgressFactory createLogProgressMonitorFactory(MonitorType mt) {
        switch (mt) {
            case STREAM:
                return new DefaultNutsInputStreamProgressFactory();
            case DEFAULT:
                return new DefaultNutsProgressFactory();
        }
        return new DefaultNutsProgressFactory();
    }

    public static NutsProgressMonitor createProgressMonitor(MonitorType mt, Object source, Object sourceOrigin, NutsSession session,
                                                            boolean logProgress,
                                                            boolean traceProgress,
                                                            NutsProgressFactory progressFactory) {
        List<NutsProgressMonitor> all = new ArrayList<>();
        if (logProgress) {
            NutsProgressMonitor e = createLogProgressMonitorFactory(mt).create(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (traceProgress) {
            all.add(new TraceNutsProgressMonitor());
        }
        if (progressFactory != null) {
            NutsProgressMonitor e = progressFactory.create(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (all.isEmpty()) {
            return new SilentNutsProgressMonitor();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsProgressMonitorList(all.toArray(new NutsProgressMonitor[0]));
    }

    public enum MonitorType {
        STREAM,
        DEFAULT,
    }
}
