package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NOutputStreamAdapter;

import java.io.PrintStream;

public class PrintStreamFromNOutputStream extends PrintStream implements NOutputStreamAdapter {
    private NPrintStream base;

    public PrintStreamFromNOutputStream(OutputStreamFromNOutputStream out) {
        super(out);
        this.base = out.getBaseOutputStream();
    }

    public PrintStreamFromNOutputStream(NPrintStream out) {
        super(new OutputStreamFromNOutputStream(out));
        this.base = base;
    }

    @Override
    public NPrintStream getBaseOutputStream() {
        return base;
    }
}
