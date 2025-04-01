package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;

import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {

    public AbstractSyncIProcessExecHelper() {
    }

    @Override
    public Future<Integer> execAsync() {
        return NScheduler.of().executorService().submit(this::exec);
    }
}
