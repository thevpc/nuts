package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;

import java.nio.file.Path;

public interface NutsContentTypeResolver extends NutsComponent {
    NutsSupported<String> probeContentType(NutsPath path, NutsSession session);

    NutsSupported<String> probeContentType(byte[] bytes, String name, NutsSession session);
}
