package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.io.FilePath;
import net.thevpc.nuts.runtime.core.io.NutsPathFromSPI;
import net.thevpc.nuts.runtime.core.io.URLPath;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsPaths implements NutsPaths {
    private final NutsWorkspace ws;

    public DefaultNutsPaths(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsPath createPath(String path,NutsSession session) {
        checkSession(session);
        return createPath(path, null,session);
    }

    @Override
    public NutsPath createPath(File path,NutsSession session) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return new NutsPathFromSPI(new FilePath(path.toPath(), session));
    }

    @Override
    public NutsPath createPath(Path path,NutsSession session) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return new NutsPathFromSPI(new FilePath(path, session));
    }

    @Override
    public NutsPath createPath(URL path,NutsSession session) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return new NutsPathFromSPI(new URLPath(path, session));
    }

    @Override
    public NutsPath createPath(String path, ClassLoader classLoader,NutsSession session) {
        checkSession(session);
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NutsPath p = getModel(session).resolve(path, session, classLoader);
        if (p == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NutsPaths addPathFactory(NutsPathFactory pathFactory,NutsSession session) {
        getModel(session).addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NutsPaths removePathFactory(NutsPathFactory pathFactory,NutsSession session) {
        getModel(session).removePathFactory(pathFactory);
        return this;
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    private DefaultNutsWorkspaceConfigModel getModel(NutsSession session) {
        return ((DefaultNutsWorkspaceConfigManager) session.config()).getModel();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
