package net.vpc.app.nuts;

public class NutsContent {
    private String file;
    private boolean cached;
    private boolean temporary;

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
