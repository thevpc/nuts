package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsPrintStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNutsPrintStream extends PrintStream implements NutsPrintStreamAdapter {
    private NutsPrintStream base;

    public PrintStreamFromNutsPrintStream(OutputStreamFromNutsPrintStream out) {
        super(out);
        this.base = out.getBaseNutsPrintStream();
    }

    public PrintStreamFromNutsPrintStream(NutsPrintStream out) {
        super(new OutputStreamFromNutsPrintStream(out));
        this.base = base;
    }

    @Override
    public NutsPrintStream getBaseNutsPrintStream() {
        return base;
    }
}
