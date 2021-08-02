package net.thevpc.nuts.toolbox.nadmin;

import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfo;

import java.nio.file.Path;

public class PathInfo {
    private NdiScriptInfo.Type type;
    private Path path;
    private Status status;

    public PathInfo(NdiScriptInfo.Type type,Path path, Status status) {
        this.type = type;
        this.path = path;
        this.status = status;
    }

    public NdiScriptInfo.Type getType() {
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
