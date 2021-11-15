package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

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
        return CoreIOUtils.getURLName(path);
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
        return new NutsPathFromSPI(new ClassLoaderPath(other, loader, getSession()));
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
}
