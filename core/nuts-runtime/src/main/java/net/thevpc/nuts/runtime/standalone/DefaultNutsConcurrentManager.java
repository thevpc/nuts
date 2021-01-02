package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsConcurrentManager;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOLockAction;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultNutsConcurrentManager implements NutsConcurrentManager {
    private NutsWorkspace workspace;
    private ExecutorService executorService;
    public DefaultNutsConcurrentManager(NutsWorkspace workspace) {
        this.workspace=workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public ExecutorService executorService() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = workspace.config().options().getExecutorService();
                    if (executorService == null) {
                        ThreadPoolExecutor executorService2 = (ThreadPoolExecutor) Executors.newCachedThreadPool(CoreNutsUtils.nutsDefaultThreadFactory);
                        executorService2.setKeepAliveTime(60, TimeUnit.SECONDS);
                        executorService2.setMaximumPoolSize(60);
                        executorService = executorService2;
                    }
                }
            }
        }
        return executorService;
    }


    public DefaultNutsIOLockAction lock() {
        return new DefaultNutsIOLockAction(workspace);
    }

}
