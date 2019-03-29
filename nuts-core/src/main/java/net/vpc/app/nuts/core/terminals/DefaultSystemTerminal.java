package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {
    private NutsSystemTerminalBase base;

    public DefaultSystemTerminal(NutsSystemTerminalBase base) {
        this.base = base;
    }

    @Override
    public NutsSystemTerminalBase getParent() {
        return base;
    }
}
