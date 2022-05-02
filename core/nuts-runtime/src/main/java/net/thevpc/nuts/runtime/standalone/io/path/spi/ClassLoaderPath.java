package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.util.Objects;

public class ClassLoaderPath extends URLPath {
    private final String path;
    private final ClassLoader loader;

    public ClassLoaderPath(String path, ClassLoader loader, NutsSession session) {
        super(loader.getResource(path.substring("classpath://".length())), session, true);
        this.path = path;
        this.loader = loader;
        if (!path.startsWith("classpath://")) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid classpath url format: %s", path)
            );
        }
    }

    @Override
    public String toString() {
        return path;
    }

    public String getName(NutsPath basePath) {
        return URLPath.getURLName(path);
    }

    @Override
    public String getLocation(NutsPath basePath) {
        if (url != null) {
            return super.getLocation(basePath);
        }
        return path.substring("classpath:/".length());
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "classpath";
    }

    protected NutsPath rebuildURLPath(String other) {
        return NutsPath.of(new ClassLoaderPath(other, loader, getSession()),getSession());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
        ClassLoaderPath that = (ClassLoaderPath) o;
        return Objects.equals(path, that.path) && Objects.equals(loader, that.loader);
    }

    @Override
    public int hashCode() {
//        return Objects.hash(super.hashCode(), path, loader);
        return Objects.hash(path, loader);
    }

    public static class ClasspathFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public ClasspathFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(ws, session);
            try {
                if (path.startsWith("classpath:")) {
                    return NutsSupported.of(10,()->new ClassLoaderPath(path, classLoader, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }
}
