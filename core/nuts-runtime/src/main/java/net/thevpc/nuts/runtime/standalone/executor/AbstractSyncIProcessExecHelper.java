package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;

import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {
    private NSession session;

    public AbstractSyncIProcessExecHelper(NSession session) {
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public Future<Integer> execAsync() {
        return NScheduler.of(getSession()).executorService().submit(this::exec);
    }
}
