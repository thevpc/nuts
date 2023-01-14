package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;

public class NPrintStreamHelper implements OutputHelper {
    private NPrintStream out;

    public NPrintStreamHelper(NPrintStream out) {
        this.out = out;
    }

    @Override
    public void write(byte[] b, int offset, int len) {
        out.print(b, offset, len);
    }

    @Override
    public void flush() {
        out.flush();
    }

    public NPrintStream out() {
        return out;
    }
}
