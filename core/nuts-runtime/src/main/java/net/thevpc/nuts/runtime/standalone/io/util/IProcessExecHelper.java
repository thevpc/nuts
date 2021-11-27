package net.thevpc.nuts.runtime.standalone.io.util;

import java.util.concurrent.Future;

public interface IProcessExecHelper {
    void dryExec();
    int exec();
    Future<Integer> execAsync();
}
