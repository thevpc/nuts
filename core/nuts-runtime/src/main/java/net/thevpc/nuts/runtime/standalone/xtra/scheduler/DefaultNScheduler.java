package net.thevpc.nuts.runtime.standalone.xtra.scheduler;

import net.thevpc.nuts.NSupported;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.concurrent.ExecutorService;

public class DefaultNScheduler implements NScheduler {
    private NSession session;

    public DefaultNScheduler(NSession session) {
        this.session = session;
    }


    @Override
    public ExecutorService executorService() {
        return NWorkspaceExt.of(session).getModel().configModel.executorService(session);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NSupported.DEFAULT_SUPPORT;
    }
}
