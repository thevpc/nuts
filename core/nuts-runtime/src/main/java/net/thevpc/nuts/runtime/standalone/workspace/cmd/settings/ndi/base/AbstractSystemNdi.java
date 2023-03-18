package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;

public abstract class AbstractSystemNdi implements SystemNdi {
    protected NSession session;

    public AbstractSystemNdi(NSession session) {
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }
}
