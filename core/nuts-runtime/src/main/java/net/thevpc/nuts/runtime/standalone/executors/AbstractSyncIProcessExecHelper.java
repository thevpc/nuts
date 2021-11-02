package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.io.IProcessExecHelper;

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
        return session.config().executorService().submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return exec();
            }
        });
    }
}
