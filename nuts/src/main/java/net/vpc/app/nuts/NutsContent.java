package net.vpc.app.nuts;

import java.nio.file.Path;

public class NutsContent {
    private final Path file;
    private final boolean cached;
    private final boolean temporary;

    public NutsContent(Path file, boolean cached, boolean temporary) {
        this.file = file;
        this.cached = cached;
        this.temporary = temporary;
    }

    public Path getPath() {
        return file;
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isTemporary() {
        return temporary;
    }
}
