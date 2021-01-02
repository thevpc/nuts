package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.NutsTerminalMode;

public enum NutsTerminalModeOp {
    NOP(NutsTerminalMode.INHERITED, NutsTerminalMode.INHERITED),
    FILTER(NutsTerminalMode.FORMATTED, NutsTerminalMode.INHERITED),
    FORMAT(NutsTerminalMode.FORMATTED, NutsTerminalMode.INHERITED),
    ESCAPE(NutsTerminalMode.FORMATTED, NutsTerminalMode.FORMATTED),
    UNESCAPE(NutsTerminalMode.FORMATTED, NutsTerminalMode.FORMATTED);
    private NutsTerminalMode in;
    private NutsTerminalMode out;

    NutsTerminalModeOp(NutsTerminalMode in, NutsTerminalMode out) {
        this.in = in;
        this.out = out;
    }

    public NutsTerminalMode in() {
        return in;
    }

    public NutsTerminalMode out() {
        return out;
    }
}
