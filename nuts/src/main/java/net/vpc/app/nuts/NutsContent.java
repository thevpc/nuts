package net.vpc.app.nuts;

public class NutsContent {
    private final String file;
    private final boolean cached;
    private final boolean temporary;

    public NutsContent(String file, boolean cached, boolean temporary) {
        this.file = file;
        this.cached = cached;
        this.temporary = temporary;
    }

    public String getFile() {
        return file;
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isTemporary() {
        return temporary;
    }
}
