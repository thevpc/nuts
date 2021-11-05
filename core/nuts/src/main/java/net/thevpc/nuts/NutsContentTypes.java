package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.InputStream;
import java.nio.file.Path;

public interface NutsContentTypes extends NutsComponent {
    static NutsContentTypes of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsContentTypes.class, true, null);
    }

    String probeContentType(Path path);

    String probeContentType(NutsPath path);

    String probeContentType(InputStream stream,String name);

    String probeContentType(byte[] stream,String name);
}
