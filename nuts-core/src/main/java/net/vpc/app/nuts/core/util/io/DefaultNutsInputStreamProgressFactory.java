package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.CoreNutsConstants;
import net.vpc.app.nuts.core.io.DefaultNutsInputStreamProgressMonitor;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNutsInputStreamProgressFactory implements NutsInputStreamProgressFactory {
    private static final Logger LOG = Logger.getLogger(DefaultNutsInputStreamProgressFactory.class.getName());

    @Override
    public NutsInputStreamProgressMonitor create(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, sourceName, session)) {
            return null;
        }
        return new DefaultNutsInputStreamProgressMonitor();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        String path = null;
        if (source instanceof String) {
            path = (String) source;
        }
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = session.getWorkspace().commandLine().create(String.valueOf(o)).next().getBoolean();
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (monitorable) {
            if (sourceOrigin instanceof NutsId) {
                NutsId d = (NutsId) sourceOrigin;
                if (NutsConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
            if (monitorable) {
                if (path != null) {
                    if (path.endsWith("/" + CoreNutsConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNutsConstants.Files.DOT_FILES)
                            || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                            || path.endsWith(".xml") || path.endsWith(".json")) {
                        monitorable = false;
                    }
                }
            }
        }
        if (!CoreCommonUtils.getSysBoolNutsProperty("monitor.enabled", true)) {
            monitorable = false;
        }
        if (!LOG.isLoggable(Level.INFO)) {
            monitorable = false;
        }
        if (!session.isPlainOut()) {
            monitorable = false;
        }
        return monitorable;
    }
}
