package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.net.URL;
import java.util.Objects;

public class ClassLoaderPath extends NutsPathBase {
    private String path;
    private URL url;

    public ClassLoaderPath(String path, ClassLoader loader, NutsSession session) {
        super(session);
        this.path = path;
        this.url = loader.getResource(path);
    }

    public String getName() {
        return CoreIOUtils.getURLName(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassLoaderPath that = (ClassLoaderPath) o;
        return Objects.equals(path, that.path) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, url);
    }

    @Override
    public String toString() {
        return url != null ?
                "classpath:" + url.toString()
                : "broken-classpath:" + path;
    }
}
