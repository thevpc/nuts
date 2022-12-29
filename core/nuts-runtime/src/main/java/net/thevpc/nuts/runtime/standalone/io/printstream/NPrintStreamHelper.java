package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;

public class NPrintStreamHelper implements OutputHelper {
    private NStream out;

    public NPrintStreamHelper(NStream out) {
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

    public NStream out() {
        return out;
    }
}
