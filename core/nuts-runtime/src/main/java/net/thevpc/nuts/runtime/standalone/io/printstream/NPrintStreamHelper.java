package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;

public class NPrintStreamHelper implements OutputHelper {
    private NOutStream out;

    public NPrintStreamHelper(NOutStream out) {
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

    public NOutStream out() {
        return out;
    }
}
