package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.NNonBlockingInputStreamAdapter;

import java.io.File;
import java.io.OutputStream;

public class NExecOutput2 {
    NExecOutput base;
    File file;
    OutputStream tempStream;
    NPath tempPath;

    private NNonBlockingInputStreamAdapter termIn = null;
    public NExecOutput2(NExecOutput base) {
        this.base = base;
    }
}
