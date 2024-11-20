package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;

import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {
    protected NWorkspace workspace;

    public AbstractSyncIProcessExecHelper(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public Future<Integer> execAsync() {
        return NScheduler.of().executorService().submit(this::exec);
    }
}
