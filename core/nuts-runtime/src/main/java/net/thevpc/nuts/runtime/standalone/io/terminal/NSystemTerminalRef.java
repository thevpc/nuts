package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NEvents;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspaceEvent;
import net.thevpc.nuts.NWorkspaceListener;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTextStyles;

public class NSystemTerminalRef extends AbstractSystemTerminalAdapter {

    private NSystemTerminalBase base;
    private NSystemTerminalBase defaultVal;

    public NSystemTerminalRef(NSystemTerminalBase base) {
        this.base = base;
        this.defaultVal = base;
    }

    @Override
    public NSystemTerminalBase getBase() {
        return base;
    }

    public NSystemTerminalRef setBase(NSystemTerminalBase base, NSession session) {
        NSystemTerminalBase old = this.base;
        if (base == null) {
            this.base = defaultVal;
        } else {
            this.base = base;
        }

        if (old != base) {
            NWorkspaceEvent event = null;
            if (session != null) {
                for (NWorkspaceListener workspaceListener : NEvents.of(session).getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNWorkspaceEvent(session, null, "systemTerminal", null, this);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }

        return this;
    }

    @Override
    public void setStyles(NTextStyles styles, NSession session) {
        base.setStyles(styles, session);
    }
}
