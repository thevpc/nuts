package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NOutputStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNOutputStream extends PrintStream implements NOutputStreamAdapter {
    private NOutputStream base;

    public PrintStreamFromNOutputStream(OutputStreamFromNOutputStream out) {
        super(out);
        this.base = out.getBaseOutputStream();
    }

    public PrintStreamFromNOutputStream(NOutputStream out) {
        super(new OutputStreamFromNOutputStream(out));
        this.base = base;
    }

    @Override
    public NOutputStream getBaseOutputStream() {
        return base;
    }
}
