package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsQuestionExecutor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class DefaultSystemTerminal extends AbstractSystemTerminalAdapter {
    private NutsSystemTerminalBase base;

    public DefaultSystemTerminal(NutsSystemTerminalBase base) {
        this.base = base;
    }

    public NutsSystemTerminalBase getParent() {
        return base;
    }
}
