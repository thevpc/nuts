package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

public class ClassLoaderPath extends URLPath {
    private String path;

    public ClassLoaderPath(String path, ClassLoader loader, NutsSession session) {
        super(loader.getResource(path),session,true);
        this.path = path;
    }

    public String name() {
        return CoreIOUtils.getURLName(path);
    }

    @Override
    public String asString() {
        return path;
    }

    @Override
    public String toString() {
        return toURL() != null ?
                "classpath:" + toURL().toString()
                : "broken-classpath:" + path;
    }
}
