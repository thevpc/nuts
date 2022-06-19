package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsInputSource;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsInputSourceMetadata;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NutsProgressUtils {
    public static java.io.InputStream monitor(URL from, NutsProgressListener monitor, NutsSession session) {
        return monitor(
                NutsPath.of(from, session).getInputStream(),
                from, NutsTexts.of(session).ofStyled(
                        NutsPath.of(from, session).getName()
                        , NutsTextStyle.path()),
                NutsPath.of(from, session).getContentLength(), monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source,
                                              NutsString sourceName, long length, NutsProgressListener monitor, NutsSession session) {
        return new MonitoredInputStream(from, source, NutsMessage.ofNtf(sourceName), length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source,
                                              NutsMessage sourceName, long length, NutsProgressListener monitor, NutsSession session) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsProgressListener monitor, NutsSession session) {
        NutsString sourceName = null;
        long length = -1;
        NutsInputSourceMetadata m = (from instanceof NutsInputSource) ? ((NutsInputSource) from).getInputMetaData() : null;
        if (m != null) {
            sourceName = NutsTexts.of(session).ofText(m.getName());
            length = m.getContentLength().orElse(-1L);
        }
        return new MonitoredInputStream(from, source, NutsMessage.ofNtf(sourceName), length, monitor, session);
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

    public static NutsProgressListener createProgressMonitor(MonitorType mt, NutsInputSource source, Object sourceOrigin, NutsSession session,
                                                             boolean logProgress,
                                                             boolean traceProgress,
                                                             NutsProgressFactory progressFactory) {
        List<NutsProgressListener> all = new ArrayList<>();
        if (logProgress) {
            NutsProgressListener e = createLogProgressMonitorFactory(mt).createProgressListener(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (traceProgress && session.isProgress()) {
            all.add(new TraceNutsProgressListener());
        }
        if (progressFactory != null) {
            NutsProgressListener e = progressFactory.createProgressListener(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (all.isEmpty()) {
            return new SilentNutsProgressListener();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsProgressListenerList(all.toArray(new NutsProgressListener[0]));
    }

    public enum MonitorType {
        STREAM,
        DEFAULT,
    }
}
