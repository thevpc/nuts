package net.thevpc.nuts.runtime.standalone.wscommands.settings;

import net.thevpc.nuts.NutsId;

import java.nio.file.Path;

public class PathInfo {
    private PathInfoType type;
    private NutsId id;
    private Path path;
    private Status status;

    public PathInfo(PathInfoType type, NutsId id, Path path, Status status) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.status = status;
    }

    public NutsId getId() {
        return id;
    }

    public PathInfoType getType() {
        return type;
    }

    public Path getPath() {
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