package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultNutsContentTypeResolver implements NutsContentTypeResolver {

    @Override
    public NutsSupported<String> probeContentType(Path path, NutsSession session) {
        String contentType = null;
        if (path != null) {
            try {
                contentType = Files.probeContentType(path);
            } catch (IOException e) {
                //ignore
            }
            if (contentType == null || "text/plain".equals(contentType)) {
                String e = NutsPath.of(path,session).getLastExtension();
                if (e != null && e.equalsIgnoreCase("ntf")) {
                    return NutsSupported.of("text/x-nuts-text-format", DEFAULT_SUPPORT + 10);
                }
            }
        }
        if (contentType != null) {
            return NutsSupported.of(contentType,DEFAULT_SUPPORT);
        }
        return NutsSupported.invalid();

    }
    public NutsSupported<String> probeContentType(NutsPath path, NutsSession session) {
        if(path!=null) {
            if(path.isFile()) {
                Path file = path.asFile();
                if(file!=null){
                    String contentType = null;
                    try {
                        contentType = Files.probeContentType(file);
                    } catch (IOException e) {
                        //ignore
                    }
                    if (contentType == null || "text/plain".equals(contentType)) {
                        String e = path.getLastExtension();
                        if (e != null && e.equalsIgnoreCase("ntf")) {
                            return NutsSupported.of("text/x-nuts-text-format", DEFAULT_SUPPORT + 10);
                        }
                    }
                    if (contentType != null) {
                        return NutsSupported.of(contentType,DEFAULT_SUPPORT);
                    }
                }
            }
        }

        return NutsSupported.invalid();
    }


    @Override
    public NutsSupported<String> probeContentType(byte[] bytes, String name, NutsSession session) {
        String contentType = null;
        if(bytes!=null) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                //ignore
            }
        }
        if (contentType == null) {
            if (name != null) {
                contentType = URLConnection.guessContentTypeFromName(name);
            }
        }
        if (contentType == null || "text/plain".equals(contentType)) {
            if (NutsBlankable.isBlank(name)) {
                String e = NutsPath.of(name,session).getLastExtension();
                if (e != null && e.equalsIgnoreCase("ntf")) {
                    return NutsSupported.of("text/x-nuts-text-format", DEFAULT_SUPPORT + 10);
                }
            }
        }
        if (contentType != null) {
            return NutsSupported.of(contentType,DEFAULT_SUPPORT);
        }
        return NutsSupported.invalid();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}

