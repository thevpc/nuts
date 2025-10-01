package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;

import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {

    public AbstractSyncIProcessExecHelper() {
    }

    @Override
    public Future<Integer> execAsync() {
        return NConcurrent.of().executorService().submit(this::exec);
    }
}
