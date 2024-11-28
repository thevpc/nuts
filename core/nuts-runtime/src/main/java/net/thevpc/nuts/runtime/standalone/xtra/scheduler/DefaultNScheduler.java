package net.thevpc.nuts.runtime.standalone.xtra.scheduler;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.concurrent.ExecutorService;

public class DefaultNScheduler implements NScheduler {

    public DefaultNScheduler() {
    }


    @Override
    public ExecutorService executorService() {
        return NWorkspaceExt.of().getModel().configModel.executorService();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
