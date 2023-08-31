//package net.thevpc.nuts.io;
//
//import net.thevpc.nuts.format.NFormat;
//import net.thevpc.nuts.util.NMsg;
//import net.thevpc.nuts.NSession;
//import net.thevpc.nuts.text.NString;
//import net.thevpc.nuts.spi.NSystemTerminalBase;
//import net.thevpc.nuts.text.NTerminalCommand;
//import net.thevpc.nuts.text.NTextStyle;
//import net.thevpc.nuts.text.NTextStyles;
//
//import java.io.*;
//import java.time.temporal.Temporal;
//import java.util.Date;
//
//public class NPlainOutputStream implements NOutputStream {
//    private ByteArrayOutputStream sb = new ByteArrayOutputStream();
//    private DefaultNContentMetadata md = new DefaultNContentMetadata();
//
//    @Override
//    public OutputStream getOutputStream() {
//        return new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                sb.write(b);
//            }
//        };
//    }
//
//    @Override
//    public NTerminalMode getTerminalMode() {
//        return NTerminalMode.INHERITED;
//    }
//
//    @Override
//    public boolean isAutoFlash() {
//        return false;
//    }
//
//    @Override
//    public NOutputStream setTerminalMode(NTerminalMode other) {
//        return null;
//    }
//
//    @Override
//    public OutputStream asOutputStream(NSession session) {
//        return new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                sb.write(b);
//            }
//        };
//    }
//
//    @Override
//    public NContentMetadata getMetaData() {
//        return md;
//    }
//
//    @Override
//    public NOutputStream flush(NSession session) {
//        return this;
//    }
//
//    @Override
//    public NOutputStream close(NSession session) {
//        return this;
//    }
//
//    @Override
//    public NOutputStream write(byte[] b, NSession session) {
//        try {
//            sb.write(b);
//        } catch (IOException e) {
//            throw new NIOException(session, e);
//        }
//        return this;
//    }
//
//    @Override
//    public NOutputStream write(int b, NSession session) {
//        sb.write(b);
//        return this;
//    }
//
//    @Override
//    public boolean isNtf() {
//        return false;
//    }
//
//    public String toString() {
//        return sb.toString();
//    }
//
//    @Override
//    public NOutputStream write(byte[] buf, int off, int len, NSession session) {
//        sb.write(buf, off, len);
//        return this;
//    }
//
//    @Override
//    public NOutputStream write(char[] buf, int off, int len, NSession session) {
//        try {
//            sb.write(new String(buf, off, len).getBytes());
//        } catch (IOException e) {
//            throw new NIOException(session, e);
//        }
//        return this;
//    }
//
//
//    @Override
//    public NOutputStream writeRaw(byte[] buf, int off, int len, NSession session) {
//        sb.write(buf, off, len);
//        return this;
//    }
//
//    @Override
//    public NFormat formatter(NSession session) {
//        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "print-stream"));
//    }
//}
