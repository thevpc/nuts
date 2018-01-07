package net.vpc.app.nuts;

import java.io.File;

/**
 * Created by vpc on 6/23/17.
 */
public class NutsUpdate {
    private File oldIdFile;
    private File availableIdFile;
    private NutsId baseId;
    private NutsId localId;
    private NutsId availableId;
    private boolean runtime;

    public NutsUpdate(NutsId baseId, NutsId localId, NutsId availableId,File oldIdFile,File availableIdFile,boolean runtime) {
        this.baseId = baseId;
        this.localId = localId;
        this.availableId = availableId;
        this.availableIdFile = availableIdFile;
        this.oldIdFile = oldIdFile;
        this.runtime = runtime;
    }

    public File getOldIdFile() {
        return oldIdFile;
    }

    public File getAvailableIdFile() {
        return availableIdFile;
    }

    public boolean isRuntime() {
        return runtime;
    }

    public NutsId getBaseId() {
        return baseId;
    }

    public NutsId getLocalId() {
        return localId;
    }

    public NutsId getAvailableId() {
        return availableId;
    }
}
