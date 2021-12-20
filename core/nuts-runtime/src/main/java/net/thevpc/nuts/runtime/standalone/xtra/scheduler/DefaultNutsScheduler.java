package net.thevpc.nuts.runtime.standalone.xtra.scheduler;

import net.thevpc.nuts.NutsScheduler;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.concurrent.ExecutorService;

public class DefaultNutsScheduler implements NutsScheduler {
    private NutsSession session;

    public DefaultNutsScheduler(NutsSession session) {
        this.session = session;
    }


    @Override
    public ExecutorService executorService() {
        return NutsWorkspaceExt.of(session).getModel().configModel.executorService(session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
