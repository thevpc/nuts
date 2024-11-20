package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.spi.NSystemTerminalBase;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {

    private final NSystemTerminalBase base;

    public DefaultSystemTerminal(NWorkspace workspace,NSystemTerminalBase base) {
        super(workspace);
        this.base = base;
    }

    @Override
    public NSystemTerminalBase getBase() {
        return base;
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        base.setStyles(styles, printStream);
    }
}
