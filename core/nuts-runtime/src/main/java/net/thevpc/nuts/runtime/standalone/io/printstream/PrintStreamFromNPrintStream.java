package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNPrintStream extends PrintStream implements NPrintStreamAdapter {
    private NOutStream base;

    public PrintStreamFromNPrintStream(OutputStreamFromNPrintStream out) {
        super(out);
        this.base = out.getBasePrintStream();
    }

    public PrintStreamFromNPrintStream(NOutStream out) {
        super(new OutputStreamFromNPrintStream(out));
        this.base = base;
    }

    @Override
    public NOutStream getBasePrintStream() {
        return base;
    }
}
