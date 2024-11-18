package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;

public abstract class AbstractSystemNdi implements SystemNdi {
    protected NWorkspace workspace;

    public AbstractSystemNdi(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }
}
