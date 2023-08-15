package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

public class DefaultNInputStreamProgressFactory implements NProgressFactory {

    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin, NSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new TraceNProgressListener();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NSession session) {
        if (!session.isProgress()) {
            return false;
        }
        if (sourceOrigin instanceof NId) {
            NId d = (NId) sourceOrigin;
            if (NConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                return false;
            }
            if (NConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                return false;
            }
        }
        if (source instanceof String) {
            String path = null;
            path = (String) source;
            if (path.endsWith("/" + CoreNConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNConstants.Files.DOT_FILES)
                    || path.endsWith(".pom") || path.endsWith(NConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                    || path.endsWith(".xml") || path.endsWith(".json")) {
                return false;
            }
        }
        return true;
    }
}
