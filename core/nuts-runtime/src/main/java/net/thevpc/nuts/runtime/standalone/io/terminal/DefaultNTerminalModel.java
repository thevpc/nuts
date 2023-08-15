package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;

public class DefaultNTerminalModel {

    private NWorkspace ws;
    private NLog LOG;

    public DefaultNTerminalModel(NWorkspace ws) {
        this.ws = ws;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNTerminalModel.class,session);
        }
        return LOG;
    }

    public NWorkspace getWorkspace() {
        return ws;
    }




}
