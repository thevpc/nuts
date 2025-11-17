package net.thevpc.nuts.runtime.standalone.io.printstream;


import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class NNonClosablePrintStream extends PrintStream {
    public NNonClosablePrintStream(OutputStream out) {
        super(asNNonClosableOutputStream(out));
    }

    public NNonClosablePrintStream(OutputStream out, boolean autoFlush) {
        super(asNNonClosableOutputStream(out), autoFlush);
    }

    public NNonClosablePrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(asNNonClosableOutputStream(out), autoFlush, encoding);
    }
    private static NNonClosableOutputStream asNNonClosableOutputStream(OutputStream out) {
        if(out==null){
            return null;
        }
        if(out instanceof NNonClosableOutputStream){
            return (NNonClosableOutputStream) out;
        }
        return new NNonClosableOutputStream(out);
    }

    @Override
    public void close() {
        //never close!!
        flush();
    }
}
