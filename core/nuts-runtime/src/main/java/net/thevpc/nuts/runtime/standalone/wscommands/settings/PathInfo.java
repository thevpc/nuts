package net.thevpc.nuts.runtime.standalone.wscommands.settings;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptInfoType;

import java.nio.file.Path;

public class PathInfo {
    private NdiScriptInfoType type;
    private NutsId id;
    private Path path;
    private Status status;

    public PathInfo(NdiScriptInfoType type, NutsId id, Path path, Status status) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.status = status;
    }

    public NutsId getId() {
        return id;
    }

    public NdiScriptInfoType getType() {
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
