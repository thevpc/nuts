package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOLockAction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultNutsUtilModel {
    private NutsWorkspace workspace;
    public DefaultNutsUtilModel(NutsWorkspace workspace) {
        this.workspace=workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}
