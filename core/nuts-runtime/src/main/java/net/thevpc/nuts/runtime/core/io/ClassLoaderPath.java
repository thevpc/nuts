package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

public class ClassLoaderPath extends URLPath {
    private String path;

    public ClassLoaderPath(String path, ClassLoader loader, NutsSession session) {
        super(loader.getResource(path.substring("classpath://".length())), session, true);
        this.path = path;
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

    public String getName() {
        return CoreIOUtils.getURLName(path);
    }

    @Override
    public String asString() {
        return path;
    }

    @Override
    public String getLocation() {
        if (url != null) {
            return super.getLocation();
        }
        return path.substring("classpath:/".length());
    }
}
