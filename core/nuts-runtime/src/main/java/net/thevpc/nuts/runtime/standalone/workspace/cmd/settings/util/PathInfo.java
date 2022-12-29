package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;

public class PathInfo {
    private String type;
    private NId id;
    private NPath path;
    private Status status;

    public PathInfo(String type, NId id, NPath path, Status status) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.status = status;
    }

    public NId getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public NPath getPath() {
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
