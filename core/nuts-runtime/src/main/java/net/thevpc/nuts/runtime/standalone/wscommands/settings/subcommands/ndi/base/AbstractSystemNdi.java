package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.SystemNdi;

import java.util.logging.Logger;

public abstract class AbstractSystemNdi implements SystemNdi {
    public static final Logger LOG = Logger.getLogger(AbstractSystemNdi.class.getName());
    protected NutsSession session;

    public AbstractSystemNdi(NutsSession appContext) {
        this.session = appContext;
    }

    public NutsSession getSession() {
        return session;
    }
}
