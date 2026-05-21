package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.spi.NSystemTerminalBase;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {

    private final NSystemTerminalBase base;

    public DefaultSystemTerminal(NSystemTerminalBase base) {
        super();
        this.base = base;
    }

    @Override
    public NSystemTerminalBase base() {
        return base;
    }

    @Override
    public void styles(NTextStyles styles, NPrintStream printStream) {
        base.styles(styles, printStream);
    }
}
