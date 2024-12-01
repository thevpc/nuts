package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTextStyles;

public class NSystemTerminalRef extends AbstractSystemTerminalAdapter {

    private NSystemTerminalBase base;
    private NSystemTerminalBase defaultVal;

    public NSystemTerminalRef(NWorkspace workspace,NSystemTerminalBase base) {
        super(workspace);
        this.base = base;
        this.defaultVal = base;
    }

    @Override
    public NSystemTerminalBase getBase() {
        return base;
    }

    public NSystemTerminalRef setBase(NSystemTerminalBase base) {
        NSession session = getWorkspace().currentSession();
        NSystemTerminalBase old = this.base;
        if (base == null) {
            this.base = defaultVal;
        } else {
            this.base = base;
        }

        if (old != base) {
            NWorkspaceEvent event = null;
            for (NWorkspaceListener workspaceListener : getWorkspace().getWorkspaceListeners()) {
                if (event == null) {
                    event = new DefaultNWorkspaceEvent(session, null, "systemTerminal", null, this);
                }
                workspaceListener.onUpdateProperty(event);
            }
        }

        return this;
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        base.setStyles(styles, printStream);
    }
}
