package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import net.thevpc.nuts.io.NPath;

import java.io.File;

public class NExecInput2 {
    NExecInput base;
    File file;
    NPath tempPath;
    NNonBlockingInputStream termIn;

    public NExecInput2(NExecInput base) {
        this.base = base;
    }
}
