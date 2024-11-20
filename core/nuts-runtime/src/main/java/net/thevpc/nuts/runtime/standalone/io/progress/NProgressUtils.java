package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

import java.util.ArrayList;
import java.util.List;

public class NProgressUtils {
//    public static java.io.InputStream ofMonitored(URL from, NProgressListener monitor, NSession session) {
//        return ofMonitored(
//                NPath.of(from, session).getInputStream(),
//                from, NTexts.of(session).ofStyled(
//                        NPath.of(from, session).getName()
//                        , NTextStyle.path()),
//                NPath.of(from, session).getContentLength(), monitor, session);
//    }


    public static NProgressFactory createLogProgressMonitorFactory(MonitorType mt) {
        NWorkspace workspace = NWorkspace.of().get();
        switch (mt) {
            case STREAM:
                return new DefaultNInputStreamProgressFactory(workspace);
            case DEFAULT:
                return new DefaultNProgressFactory(workspace);
        }
        return new DefaultNProgressFactory(workspace);
    }

    public static NProgressListener createProgressMonitor(MonitorType mt, NInputSource source, Object sourceOrigin, NWorkspace workspace,
                                                          boolean logProgress,
                                                          boolean traceProgress,
                                                          NProgressFactory progressFactory) {
        List<NProgressListener> all = new ArrayList<>();
        if (logProgress) {
            NProgressListener e = createLogProgressMonitorFactory(mt).createProgressListener(source, sourceOrigin);
            if (e != null) {
                all.add(e);
            }
        }
        if (traceProgress && workspace.currentSession().isProgress()) {
            all.add(new TraceNProgressListener());
        }
        if (progressFactory != null) {
            NProgressListener e = progressFactory.createProgressListener(source, sourceOrigin);
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
