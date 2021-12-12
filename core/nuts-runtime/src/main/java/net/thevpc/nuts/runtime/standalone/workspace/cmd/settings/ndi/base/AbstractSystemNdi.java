package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;

import java.util.logging.Logger;

public abstract class AbstractSystemNdi implements SystemNdi {
    protected NutsSession session;

    public AbstractSystemNdi(NutsSession appContext) {
        this.session = appContext;
    }

    public NutsSession getSession() {
        return session;
    }
}
