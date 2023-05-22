package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNPrintStream extends PrintStream implements NPrintStreamAdapter {
    private NPrintStream base;

    public PrintStreamFromNPrintStream(OutputStreamFromNPrintStream out) {
        super(out);
        this.base = out.getBasePrintStream();
    }

    public PrintStreamFromNPrintStream(NPrintStream out) {
        super(new OutputStreamFromNPrintStream(out));
        this.base = base;
    }

    @Override
    public NPrintStream getBasePrintStream() {
        return base;
    }
}
