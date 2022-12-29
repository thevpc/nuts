package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNPrintStream extends PrintStream implements NPrintStreamAdapter {
    private NStream base;

    public PrintStreamFromNPrintStream(OutputStreamFromNPrintStream out) {
        super(out);
        this.base = out.getBasePrintStream();
    }

    public PrintStreamFromNPrintStream(NStream out) {
        super(new OutputStreamFromNPrintStream(out));
        this.base = base;
    }

    @Override
    public NStream getBasePrintStream() {
        return base;
    }
}
