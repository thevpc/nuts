package net.thevpc.nuts.runtime.bundles.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

public interface PomUrlReader {
    final PomUrlReader DEFAULT=new PomUrlReader() {
        @Override
        public InputStream openStream(URL url) {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    };
    InputStream openStream(URL url);
}
