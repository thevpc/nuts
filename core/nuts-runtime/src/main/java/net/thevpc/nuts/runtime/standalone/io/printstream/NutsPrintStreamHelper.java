package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;

public class NutsPrintStreamHelper implements OutputHelper {
    private NutsPrintStream out;

    public NutsPrintStreamHelper(NutsPrintStream out) {
        this.out = out;
    }

    @Override
    public void write(byte[] b, int offset, int len) {
        out.write(b, offset, len);
    }

    @Override
    public void flush() {
        out.flush();
    }

    public NutsPrintStream out() {
        return out;
    }
}
