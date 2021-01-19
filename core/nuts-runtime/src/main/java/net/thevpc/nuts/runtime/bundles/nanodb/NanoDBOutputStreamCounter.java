package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class NanoDBOutputStreamCounter extends FilterOutputStream {
    private long counter;

    public NanoDBOutputStreamCounter(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        counter++;
    }

    public long getCounter() {
        return counter;
    }
}
