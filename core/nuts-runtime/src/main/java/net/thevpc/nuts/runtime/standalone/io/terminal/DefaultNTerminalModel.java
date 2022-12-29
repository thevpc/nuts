package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerOp;

public class DefaultNTerminalModel {

    private NWorkspace ws;
    private NLogger LOG;

    public DefaultNTerminalModel(NWorkspace ws) {
        this.ws = ws;
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNTerminalModel.class,session);
        }
        return LOG;
    }

    public NWorkspace getWorkspace() {
        return ws;
    }




}
