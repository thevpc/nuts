package net.thevpc.nuts.runtime.standalone.io.util;

import java.util.concurrent.Future;

public interface IProcessExecHelper {
    int exec();
    Future<Integer> execAsync();
}
