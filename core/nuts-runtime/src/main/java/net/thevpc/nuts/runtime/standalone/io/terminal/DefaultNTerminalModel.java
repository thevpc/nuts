package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;

public class DefaultNTerminalModel {

    private NWorkspace workspace;

    public DefaultNTerminalModel(NWorkspace workspace) {
        this.workspace = workspace;
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNTerminalModel.class);
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }




}
