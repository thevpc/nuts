package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.terminals.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsDefaultTerminalSpec;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalSpec;

import java.io.*;
import java.util.logging.Level;

public class DefaultNutsTerminalModel {

    private NutsWorkspace ws;
    private NutsLogger LOG;

    public DefaultNutsTerminalModel(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsTerminalModel.class,session);
        }
        return LOG;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }




}
