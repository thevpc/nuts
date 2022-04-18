package net.thevpc.nuts.runtime.standalone.xtra.execentries;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultNutsExecutionEntries implements NutsExecutionEntries {
    private final NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsExecutionEntries(NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
    }

    @Override
    public List<NutsExecutionEntry> parse(File file) {
        return parse(file.toPath());
    }

    @Override
    public List<NutsExecutionEntry> parse(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "jar", file.toAbsolutePath().normalize().toString());
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
            return Collections.emptyList();
        }
    }

    @Override
    public List<NutsExecutionEntry> parse(InputStream inputStream, String type, String sourceName) {
        if ("jar".equals(type)) {
            return JavaJarUtils.parseJarExecutionEntries(inputStream, session);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = JavaClassUtils.parseClassExecutionEntry(inputStream, sourceName, getSession());
            return u == null ? Collections.emptyList() : Arrays.asList(u);
        }
        return Collections.emptyList();
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
