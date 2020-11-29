package net.thevpc.nuts.runtime.terminals;

import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {

    private final NutsSystemTerminalBase base;

    public DefaultSystemTerminal(NutsSystemTerminalBase base) {
        this.base = base;
    }

    @Override
    public NutsSystemTerminalBase getParent() {
        return base;
    }
}
