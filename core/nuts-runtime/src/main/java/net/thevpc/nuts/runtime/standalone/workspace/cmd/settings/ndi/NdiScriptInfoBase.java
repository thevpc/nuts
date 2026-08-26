package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.io.NPath;

public abstract class NdiScriptInfoBase implements NdiScriptInfo{
    private final NPath path;

    public NdiScriptInfoBase(NPath path) {
        this.path = path;
    }

    @Override
    public NPath path() {
        return path;
    }
}
