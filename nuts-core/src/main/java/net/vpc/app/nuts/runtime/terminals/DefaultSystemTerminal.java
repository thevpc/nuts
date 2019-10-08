package net.vpc.app.nuts.runtime.terminals;

import net.vpc.app.nuts.*;

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
