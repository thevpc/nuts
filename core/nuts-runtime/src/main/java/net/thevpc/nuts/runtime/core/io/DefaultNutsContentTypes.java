package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultNutsContentTypes implements NutsContentTypes {
    private final NutsSession session;
    private NutsWorkspace ws;

    public DefaultNutsContentTypes(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

//    private Shared getShared(NutsSession session) {
//        String key = "internal:" + Shared.class.getName();
//        Shared o = (Shared) session.getWorkspace().env().getProperties().get(key);
//        if (o == null) {
//            o = new Shared();
//            session.getWorkspace().env().setProperty(key, o);
//
//        }
//        return o;
//    }


    @Override
    public String probeContentType(Path path) {
        List<NutsContentTypeResolver> allSupported = session.extensions()
                .createAllSupported(NutsContentTypeResolver.class, path);
        NutsSupported<String> best = null;
        for (NutsContentTypeResolver r : allSupported) {
            NutsSupported<String> s = r.probeContentType(path, session);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing type resolver for %s",path));
        }
        return best.getValue();
    }

    @Override
    public String probeContentType(NutsPath path) {
        List<NutsContentTypeResolver> allSupported = session.extensions()
                .createAllSupported(NutsContentTypeResolver.class, path);
        NutsSupported<String> best = null;
        for (NutsContentTypeResolver r : allSupported) {
            NutsSupported<String> s = r.probeContentType(path, session);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing type resolver for %s",path));
        }
        return best.getValue();
    }

    @Override
    public String probeContentType(InputStream stream, String name) {
        byte[] buffer=CoreIOUtils.readBestEffort(4096,stream,session);
        return probeContentType(buffer,name);
    }

    @Override
    public String probeContentType(byte[] bytes, String name) {
        Map<String,Object> constraints=new HashMap<>();
        constraints.put("bytes",bytes);
        constraints.put("name",name);
        List<NutsContentTypeResolver> allSupported = session.extensions()
                .createAllSupported(NutsContentTypeResolver.class, constraints);
        NutsSupported<String> best = null;
        for (NutsContentTypeResolver r : allSupported) {
            NutsSupported<String> s = r.probeContentType(bytes,name, session);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing type resolver for stream named %s",name));
        }
        return best.getValue();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
//
//    private static class Shared {
//
//    }
}
