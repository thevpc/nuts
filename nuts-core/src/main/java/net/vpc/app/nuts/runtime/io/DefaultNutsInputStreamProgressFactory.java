package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

import java.util.logging.Logger;

public class DefaultNutsInputStreamProgressFactory implements NutsProgressFactory {
    private static final Logger LOG = Logger.getLogger(DefaultNutsInputStreamProgressFactory.class.getName());

    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new DefaultNutsStreamProgressMonitor();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NutsSession session) {
        if (!CoreNutsUtils.acceptMonitoring(session)) {
            return false;
        }
        if (sourceOrigin instanceof NutsId) {
            NutsId d = (NutsId) sourceOrigin;
            if (NutsConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                return false;
            }
            if (NutsConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                return false;
            }
        }
        if (source instanceof String) {
            String path = null;
            path = (String) source;
            if (path.endsWith("/" + CoreNutsConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNutsConstants.Files.DOT_FILES)
                    || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                    || path.endsWith(".xml") || path.endsWith(".json")) {
                return false;
            }
        }
        return true;
    }
}
