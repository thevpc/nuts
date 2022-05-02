package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.io.NutsPath;

public class PathInfo {
    private String type;
    private NutsId id;
    private NutsPath path;
    private Status status;

    public PathInfo(String type, NutsId id, NutsPath path, Status status) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.status = status;
    }

    public NutsId getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public NutsPath getPath() {
        return path;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status{
        CREATED,
        OVERRIDDEN,
        DISCARDED,
    }
}
