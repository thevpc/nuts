//package net.thevpc.nuts.runtime.standalone.io.printstream;
//
//import net.thevpc.nuts.NSession;
//import net.thevpc.nuts.io.NOutputStream;
//import net.thevpc.nuts.io.NOutputStreamAdapter;
//import net.thevpc.nuts.io.NPrintStream;
//import net.thevpc.nuts.io.NPrintStreamAdapter;
//
//import java.io.OutputStream;
//
//public class OutputStreamFromOutputStream extends OutputStream implements NOutputStreamAdapter {
//
//    private NOutputStream base;
//
//    public OutputStreamFromOutputStream(NOutputStream base) {
//        this.base = base;
//    }
//
//    public NOutputStream getBaseOutputStream() {
//        return base;
//    }
//
//    @Override
//    public void write(int b) {
//        base.write(b,session);
//    }
//
//    @Override
//    public void write(byte[] b) {
//        base.write(b,session);
//    }
//
//    @Override
//    public void write(byte[] b, int off, int len) {
//        base.write(b, off, len,session);
//    }
//
//    @Override
//    public void flush() {
//        base.flush(session);
//    }
//
//    @Override
//    public void close() {
//        base.close(session);
//    }
//}
