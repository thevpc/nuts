package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NInputSourceMetadata;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NProgressFactory;
import net.thevpc.nuts.util.NProgressListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NProgressUtils {
    public static java.io.InputStream monitor(URL from, NProgressListener monitor, NSession session) {
        return monitor(
                NPath.of(from, session).getInputStream(),
                from, NTexts.of(session).ofStyled(
                        NPath.of(from, session).getName()
                        , NTextStyle.path()),
                NPath.of(from, session).getContentLength(), monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source,
                                              NString sourceName, long length, NProgressListener monitor, NSession session) {
        return new MonitoredInputStream(from, source, NMsg.ofNtf(sourceName), length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source,
                                              NMsg sourceName, long length, NProgressListener monitor, NSession session) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NProgressListener monitor, NSession session) {
        NString sourceName = null;
        long length = -1;
        NInputSourceMetadata m = (from instanceof NInputSource) ? ((NInputSource) from).getInputMetaData() : null;
        if (m != null) {
            sourceName = NTexts.of(session).ofText(m.getName());
            length = m.getContentLength().orElse(-1L);
        }
        return new MonitoredInputStream(from, source, NMsg.ofNtf(sourceName), length, monitor, session);
    }

    public static NProgressFactory createLogProgressMonitorFactory(MonitorType mt) {
        switch (mt) {
            case STREAM:
                return new DefaultNInputStreamProgressFactory();
            case DEFAULT:
                return new DefaultNProgressFactory();
        }
        return new DefaultNProgressFactory();
    }

    public static NProgressListener createProgressMonitor(MonitorType mt, NInputSource source, Object sourceOrigin, NSession session,
                                                          boolean logProgress,
                                                          boolean traceProgress,
                                                          NProgressFactory progressFactory) {
        List<NProgressListener> all = new ArrayList<>();
        if (logProgress) {
            NProgressListener e = createLogProgressMonitorFactory(mt).createProgressListener(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (traceProgress && session.isProgress()) {
            all.add(new TraceNProgressListener());
        }
        if (progressFactory != null) {
            NProgressListener e = progressFactory.createProgressListener(source, sourceOrigin, session);
            if (e != null) {
                all.add(e);
            }
        }
        if (all.isEmpty()) {
            return new SilentNProgressListener();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NProgressListenerList(all.toArray(new NProgressListener[0]));
    }

    public enum MonitorType {
        STREAM,
        DEFAULT,
    }
}