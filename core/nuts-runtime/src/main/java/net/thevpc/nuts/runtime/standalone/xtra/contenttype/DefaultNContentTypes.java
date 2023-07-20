package net.thevpc.nuts.runtime.standalone.xtra.contenttype;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NContentTypes;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NContentTypeResolver;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultNContentTypes implements NContentTypes {
    private final NSession session;
    private NWorkspace ws;

    public DefaultNContentTypes(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public String probeContentType(Path path) {
        return probeContentType(path == null ? null : NPath.of(path, session));
    }

    @Override
    public String probeContentType(File path) {
        return probeContentType(path == null ? null : NPath.of(path, session));
    }

    @Override
    public String probeContentType(URL path) {
        return probeContentType(path == null ? null : NPath.of(path, session));
    }

    @Override
    public String probeContentType(NPath path) {
        List<NContentTypeResolver> allSupported = session.extensions()
                .createComponents(NContentTypeResolver.class, path);
        NSupported<String> best = null;
        for (NContentTypeResolver r : allSupported) {
            NSupported<String> s = r.probeContentType(path);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            return null;
        }
        return best.getValue();
    }

    @Override
    public List<String> findExtensionsByContentType(String contentType) {
        List<NContentTypeResolver> allSupported = session.extensions()
                .createComponents(NContentTypeResolver.class, null);
        LinkedHashSet<String> all = new LinkedHashSet<>();
        for (NContentTypeResolver r : allSupported) {
            List<String> s = r.findExtensionsByContentType(contentType);
            if (s != null) {
                all.addAll(s.stream().filter(x->!NBlankable.isBlank(x)).collect(Collectors.toList()));
            }
        }
        return new ArrayList<>(all);
    }

    @Override
    public List<String> findContentTypesByExtension(String extension) {
        List<NContentTypeResolver> allSupported = session.extensions()
                .createComponents(NContentTypeResolver.class, null);
        LinkedHashSet<String> all = new LinkedHashSet<>();
        for (NContentTypeResolver r : allSupported) {
            List<String> s = r.findContentTypesByExtension(extension);
            if (s != null) {
                all.addAll(s.stream().filter(NBlankable::isBlank).collect(Collectors.toList()));
            }
        }
        return new ArrayList<>(all);
    }

    @Override
    public String probeContentType(InputStream stream) {
        byte[] buffer = CoreIOUtils.readBestEffort(4096, stream, session);
        return probeContentType(buffer);
    }

    @Override
    public String probeContentType(byte[] bytes) {
        List<NContentTypeResolver> allSupported = session.extensions()
                .createComponents(NContentTypeResolver.class, bytes);
        NSupported<String> best = null;
        for (NContentTypeResolver r : allSupported) {
            NSupported<String> s = r.probeContentType(bytes);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            return null;
        }
        return best.getValue();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NSupported.DEFAULT_SUPPORT;
    }
//
//    private static class Shared {
//
//    }
}
