package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsContentTypeResolver;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupported;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNutsContentTypeResolver implements NutsContentTypeResolver {

    public NutsSupported<String> probeContentType(NutsPath path, NutsSession session) {
        String contentType = null;
        if (path != null) {
            if (path.isFile()) {
                Path file = path.asFile();
                if (file != null) {
                    try {
                        contentType = Files.probeContentType(file);
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
            if (contentType == null) {
                URL url = path.asURL();
                try {
                    contentType = url.openConnection().getContentType();
                } catch (IOException e) {
                    //
                }

            }
            if (contentType == null) {
                NutsSupported<String> s = probeContentType(null, path.getName(), session);
                if (s.isValid()) {
                    return s;
                }
            }
            if (contentType != null) {
                return NutsSupported.of(contentType, DEFAULT_SUPPORT);
            }
        }

        return NutsSupported.invalid();
    }


    @Override
    public NutsSupported<String> probeContentType(byte[] bytes, String name, NutsSession session) {
        String contentType = null;
        if (bytes != null) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                //ignore
            }
        }
        if (contentType == null) {
            if (name != null) {
                try {
                    contentType = Files.probeContentType(Paths.get(name));
                } catch (IOException e) {
                    //ignore
                }
                if (contentType == null) {
                    try {
                        contentType = URLConnection.guessContentTypeFromName(name);
                    } catch (Exception e) {
                        //ignore
                    }
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NutsPath.of(Paths.get(name), session).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("ntf")) {
                        return NutsSupported.of("text/x-nuts-text-format", DEFAULT_SUPPORT + 10);
                    }
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NutsPath.of(Paths.get(name), session).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("nuts")) {
                        return NutsSupported.of("application/json", DEFAULT_SUPPORT + 10);
                    }
                }
            }
        }
        if (contentType != null) {
            return NutsSupported.of(contentType, DEFAULT_SUPPORT);
        }
        return NutsSupported.invalid();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}

