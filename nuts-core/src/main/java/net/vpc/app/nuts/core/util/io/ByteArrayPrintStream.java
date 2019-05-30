package net.vpc.app.nuts.core.util.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ByteArrayPrintStream extends PrintStream {
    private ByteArrayOutputStream out;

    public ByteArrayPrintStream() {
        this(new ByteArrayOutputStream());
    }

    public ByteArrayPrintStream(ByteArrayOutputStream out) {
        super(out);
        this.out = out;

    }

    public byte[] toByteArray() {
        flush();
        return out.toByteArray();
    }

    @Override
    public String toString() {
        flush();
        return out.toString();
    }

}
