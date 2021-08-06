package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractScriptBuilder implements ScriptBuilder {
    private NutsSession session;
    private NutsId anyId;
    private String path;
    private NdiScriptInfoType type;

    public AbstractScriptBuilder(NdiScriptInfoType type, NutsId anyId, NutsSession session) {
        this.session = session;
        this.anyId = anyId;
        this.type = type;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsId getAnyId() {
        return anyId;
    }

    public NdiScriptInfoType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public AbstractScriptBuilder setPath(Path path) {
        this.path = path == null ? null : path.toString();
        return this;
    }

    public AbstractScriptBuilder setPath(String preferredName) {
        this.path = preferredName;
        return this;
    }

    @Override
    public PathInfo build() {
        //Path script = getScriptFile(name);
        NutsDefinition anyIdDef = session.getWorkspace().search().addId(anyId).setLatest(true).getResultDefinitions().singleton();
        NutsId anyId = anyIdDef.getId();
        String path = NameBuilder.id(anyId,
                this.path,"%n", anyIdDef.getDescriptor(),session).buildName();
        Path script = Paths.get(path);
        String newContent = buildString();
        PathInfo.Status update0 = NdiUtils.tryWriteStatus(newContent.getBytes(), script);
        PathInfo.Status update = NdiUtils.tryWrite(newContent.getBytes(), script);
        NdiUtils.setExecutable(script);
        return new PathInfo(type, anyId, script, update);
    }
}
