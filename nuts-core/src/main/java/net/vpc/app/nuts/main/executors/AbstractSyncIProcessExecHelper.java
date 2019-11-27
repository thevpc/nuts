package net.vpc.app.nuts.main.executors;

import net.vpc.app.nuts.runtime.util.io.IProcessExecHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {
    @Override
    public Future<Integer> execAsync() {
        FutureTask<Integer> f = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return exec();
            }
        });
        new Thread(f).start();
        return f;
    }
}
