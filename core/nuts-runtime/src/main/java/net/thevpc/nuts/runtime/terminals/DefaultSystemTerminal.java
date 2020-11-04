package net.thevpc.nuts.runtime.terminals;

import net.thevpc.nuts.NutsSystemTerminalBase;

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
