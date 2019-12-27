package net.vpc.app.nuts.main.executors;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.util.io.IProcessExecHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {
    private NutsSession session;

    public AbstractSyncIProcessExecHelper(NutsSession session) {
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public Future<Integer> execAsync() {
        return session.getWorkspace().io().executorService().submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return exec();
            }
        });
    }
}
