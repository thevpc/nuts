package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsExecutionEntryManager;
import net.thevpc.nuts.NutsExecutionEntry;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultNutsExecutionEntryManager implements NutsExecutionEntryManager {
    private NutsWorkspace ws;

    public DefaultNutsExecutionEntryManager(NutsWorkspace ws) {
        this.ws = ws;
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
                throw new UncheckedIOException(ex);
            }
        } else if (file.getFileName().toString().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "class", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] parse(InputStream inputStream, String type, String sourceName) {
        if ("java".equals(type)) {
            return NutsWorkspaceUtils.of(ws).parseJarExecutionEntries(inputStream, sourceName);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = NutsWorkspaceUtils.of(ws).parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }

}
