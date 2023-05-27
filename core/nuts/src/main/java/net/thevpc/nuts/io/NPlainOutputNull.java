//package net.thevpc.nuts.io;
//
//import net.thevpc.nuts.NFormat;
//import net.thevpc.nuts.NSession;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//
//public class NPlainOutputNull implements NOutputStream {
//    private DefaultNContentMetadata md = new DefaultNContentMetadata();
//
//    @Override
//    public OutputStream getOutputStream() {
//        return new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                //
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
//        return getOutputStream();
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
//        return this;
//    }
//
//    @Override
//    public NOutputStream write(int b, NSession session) {
//        return this;
//    }
//
//    @Override
//    public boolean isNtf() {
//        return false;
//    }
//
//    public String toString() {
//        return "NPlainOutputNull";
//    }
//
//    @Override
//    public NOutputStream write(byte[] buf, int off, int len, NSession session) {
//        return this;
//    }
//
//    @Override
//    public NOutputStream write(char[] buf, int off, int len, NSession session) {
//        return this;
//    }
//
//
//    @Override
//    public NOutputStream writeRaw(byte[] buf, int off, int len, NSession session) {
//        return this;
//    }
//
//    @Override
//    public NFormat formatter(NSession session) {
//        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "print-stream"));
//    }
//}
