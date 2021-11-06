package net.thevpc.nuts.runtime.bundles.mvn;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface PomUrlReader {
    PomUrlReader DEFAULT = new PomUrlReader() {
        @Override
        public InputStream openStream(URL url, NutsSession session) {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    };

    InputStream openStream(URL url, NutsSession session);
}
