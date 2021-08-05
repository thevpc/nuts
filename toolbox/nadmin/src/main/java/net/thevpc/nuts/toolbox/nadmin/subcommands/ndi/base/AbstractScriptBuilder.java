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
        try {
            //Path script = getScriptFile(name);
            NutsDefinition anyIdDef = session.getWorkspace().search().addId(anyId).setLatest(true).getResultDefinitions().singleton();
            NutsId anyId = anyIdDef.getId();
            String path = new NameBuilder(anyId,
                    this.path == null ? "%n" : this.path
                    , anyIdDef.getDescriptor()).buildName();
            Path script = Paths.get(path);
            boolean alreadyExists = Files.exists(script);
            boolean requireWrite = false;
            String oldContent = "";
            String newContent = buildString();
            if (alreadyExists) {
                try {
                    oldContent = new String(Files.readAllBytes(script));
                } catch (Exception ex) {
                    //ignore
                }
                if (newContent.equals(oldContent)) {
                    return new PathInfo(type, anyId, script, PathInfo.Status.DISCARDED);
                }
                if (session.getTerminal().ask()
                        .resetLine()
                        .setDefaultValue(true).setSession(session)
                        .forBoolean("override existing script %s ?",
                                session.getWorkspace().text().forStyled(
                                        NdiUtils.betterPath(script.toString()), NutsTextStyle.path()
                                )
                        ).getBooleanValue()) {
                    requireWrite = true;
                }
            } else {
                requireWrite = true;
            }
            if (script.getParent() != null) {
                if (!Files.exists(script.getParent())) {
                    Files.createDirectories(script.getParent());
                }
            }
            Files.write(script, newContent.getBytes());
            NdiUtils.setExecutable(script);
            if (requireWrite) {
                if (alreadyExists) {
                    return new PathInfo(type, anyId, script, PathInfo.Status.OVERRIDDEN);
                } else {
                    return new PathInfo(type, anyId, script, PathInfo.Status.CREATED);
                }
            } else {
                return new PathInfo(type, anyId, script, PathInfo.Status.DISCARDED);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
