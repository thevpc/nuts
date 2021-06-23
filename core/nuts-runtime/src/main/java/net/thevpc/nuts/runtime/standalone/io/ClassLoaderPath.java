package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
    public String location() {
        return path;
    }

    @Override
    public String toString() {
        return toURL() != null ?
                "classpath:" + toURL().toString()
                : "broken-classpath:" + path;
    }
}
