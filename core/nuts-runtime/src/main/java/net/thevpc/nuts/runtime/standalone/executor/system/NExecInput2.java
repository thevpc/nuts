package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.NExecInput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.NonBlockingInputStreamAdapter;

import java.io.File;

public class NExecInput2 {
    NExecInput base;
    File file;
    NPath tempPath;
    NonBlockingInputStreamAdapter termIn;

    public NExecInput2(NExecInput base) {
        this.base = base;
    }
}
