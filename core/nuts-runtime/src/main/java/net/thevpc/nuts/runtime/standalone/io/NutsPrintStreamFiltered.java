package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsTerminalCommand;
import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.runtime.core.format.text.renderer.StripperFormattedPrintStreamRenderer;

public class NutsPrintStreamFiltered extends NutsPrintStreamRendered {
    private NutsPrintStreamBase base;

    public NutsPrintStreamFiltered(NutsPrintStreamBase base, Bindings bindings) {
        super(base, NutsTerminalMode.FILTERED,
                new StripperFormattedPrintStreamRenderer(),
                bindings);
        this.base = base;
        if (bindings.filtered != null) {
            throw new IllegalArgumentException("already bound ansi");
        }
        bindings.filtered = this;

    }


    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NutsPrintStreamFormatted(base, bindings);
            }
        }
        throw new IllegalArgumentException("unsupported " + mode() + "->" + other);
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command) {
        //do nothing!!
        return this;
    }
}
