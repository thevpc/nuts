package net.thevpc.nuts.toolbox.ndi.base;

import java.nio.file.Path;

public class UpdatedPaths {
    private String[] updated;
    private String[] discarded;

    public UpdatedPaths(String[] updated, String[] discarded) {
        this.updated = updated;
        this.discarded = discarded;
    }

    public String[] getUpdated() {
        return updated;
    }

    public String[] getDiscarded() {
        return discarded;
    }
}
