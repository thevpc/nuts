package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {

    private final NutsSystemTerminalBase base;

    public DefaultSystemTerminal(NutsSystemTerminalBase base) {
        this.base = base;
    }

    @Override
    public void install(NutsWorkspace workspace) {
        super.install(workspace);
    }

    @Override
    public NutsSystemTerminalBase getParent() {
        return base;
    }
}
