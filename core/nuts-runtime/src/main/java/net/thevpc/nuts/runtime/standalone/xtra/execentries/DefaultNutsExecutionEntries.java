package net.thevpc.nuts.runtime.standalone.xtra.execentries;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultNutsExecutionEntries implements NutsExecutionEntries {
    private final NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsExecutionEntries(NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
    }

    @Override
    public NutsExecutionEntry[] parse(File file) {
        return parse(file.toPath());
    }

    @Override
    public NutsExecutionEntry[] parse(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "java", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        } else if (file.getFileName().toString().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "class", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] parse(InputStream inputStream, String type, String sourceName) {
        if ("java".equals(type)) {
            return NutsWorkspaceUtils.of(getSession()).parseJarExecutionEntries(inputStream, sourceName);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = NutsWorkspaceUtils.of(getSession()).parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsExecutionEntries setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
