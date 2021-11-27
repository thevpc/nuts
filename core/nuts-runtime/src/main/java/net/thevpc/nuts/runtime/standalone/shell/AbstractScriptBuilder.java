package net.thevpc.nuts.runtime.standalone.shell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NameBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;

import java.nio.file.Path;

public abstract class AbstractScriptBuilder implements ScriptBuilder {
    private NutsSession session;
    private NutsId anyId;
    private String path;
    private String type;
    private NutsShellFamily shellFamily;

    public AbstractScriptBuilder(NutsShellFamily shellFamily,String type, NutsId anyId, NutsSession session) {
        this.session = session;
        this.shellFamily = shellFamily;
        this.anyId = anyId.builder().setRepository(null).build();//remove repo!
        this.type = type;
    }

    public NutsShellFamily getShellFamily() {
        return shellFamily;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsId getAnyId() {
        return anyId;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public AbstractScriptBuilder setPath(Path path) {
        this.path = path == null ? null : path.toString();
        return this;
    }

    public AbstractScriptBuilder setPath(NutsPath path) {
        this.path = path == null ? null : path.toString();
        return this;
    }


    public AbstractScriptBuilder setPath(String preferredName) {
        this.path = preferredName;
        return this;
    }

    public PathInfo buildAddLine(BaseSystemNdi ndi) {
        return ndi.addFileLine(type,
                anyId,
                NutsPath.of(path,session), ndi.getCommentLineConfigHeader(),buildString(),NutsShellHelper.of(getShellFamily()).getShebanSh(), shellFamily);
    }

    public PathInfo build() {
        //Path script = getScriptFile(name);
        NutsDefinition anyIdDef = session.search().addId(anyId).setLatest(true).setDistinct(true).getResultDefinitions().singleton();
        NutsId anyId = anyIdDef.getId();
        String path = NameBuilder.id(anyId,
                this.path,"%n", anyIdDef.getDescriptor(),session).buildName();
        NutsPath script = NutsPath.of(path,session);
        String newContent = buildString();
//        PathInfo.Status update0 = NdiUtils.tryWriteStatus(newContent.getBytes(), script,session);
        PathInfo.Status update = CoreIOUtils.tryWrite(newContent.getBytes(), script,session);
        script.addPermissions(NutsPathPermission.CAN_EXECUTE);
        return new PathInfo(type, anyId, script, update);
    }
}
