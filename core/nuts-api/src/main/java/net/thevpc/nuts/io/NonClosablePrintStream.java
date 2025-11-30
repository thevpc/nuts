package net.thevpc.nuts.io;


import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class NonClosablePrintStream extends PrintStream implements OutputStreamDelegate {
    private OutputStream delegated;

    public NonClosablePrintStream(OutputStream out) {
        super(asNNonClosableOutputStream(out));
        this.delegated = out;
    }

    public NonClosablePrintStream(OutputStream out, boolean autoFlush) {
        super(asNNonClosableOutputStream(out), autoFlush);
        this.delegated = out;
    }

    public NonClosablePrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(asNNonClosableOutputStream(out), autoFlush, encoding);
        this.delegated = out;
    }

    @Override
    public OutputStream getDelegateOutputStream() {
        return delegated;
    }

    private static NonClosableOutputStream asNNonClosableOutputStream(OutputStream out) {
        if (out == null) {
            return null;
        }
        if (out instanceof NonClosableOutputStream) {
            return (NonClosableOutputStream) out;
        }
        return new NonClosableOutputStream(out);
    }

    @Override
    public void close() {
        //never close!!
        flush();
    }
}
