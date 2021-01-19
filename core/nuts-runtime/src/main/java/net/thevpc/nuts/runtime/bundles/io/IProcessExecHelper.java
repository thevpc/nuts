package net.thevpc.nuts.runtime.bundles.io;

import java.util.concurrent.Future;

public interface IProcessExecHelper {
    void dryExec();
    int exec();
    Future<Integer> execAsync();
}
