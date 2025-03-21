//package net.thevpc.nuts.runtime.standalone.io.printstream;
//
//import net.thevpc.nuts.format.NFormat;
//import net.thevpc.nuts.NSession;
//import net.thevpc.nuts.io.*;
//import net.thevpc.nuts.spi.NSystemTerminalBase;
//
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.Writer;
//
//public abstract class NOutputStreamBase implements NOutputStream {
//    private static String LINE_SEP = System.getProperty("line.separator");
//    protected Bindings bindings;
//    protected OutputStream osWrapper;
//    protected PrintStream psWrapper;
//    protected Writer writerWrapper;
//    protected boolean autoFlash;
//    private NTerminalMode mode;
//    protected NSystemTerminalBase term;
//    private DefaultNContentMetadata md = new DefaultNContentMetadata();
//
//    public NOutputStreamBase(boolean autoFlash, NTerminalMode mode, Bindings bindings, NSystemTerminalBase term) {
//        this.bindings = bindings;
//        this.autoFlash = autoFlash;
//        this.mode = mode;
//        this.term = term;
//    }
//
//    public NContentMetadata getMetaData() {
//        return md;
//    }
//
//    protected abstract NOutputStream convertImpl(NTerminalMode other);
//
//    @Override
//    public String toString() {
//        return super.toString();
//    }
//
//
//    @Override
//    public NTerminalMode getTerminalMode() {
//        return mode;
//    }
//
//    @Override
//    public boolean isAutoFlash() {
//        return autoFlash;
//    }
//
//    @Override
//    public NOutputStream setTerminalMode(NTerminalMode other) {
//        if (other == null || other == this.getTerminalMode()) {
//            return this;
//        }
//        switch (other) {
//            case ANSI: {
//                if (bindings.ansi != null) {
//                    return bindings.filtered;
//                }
//                return convertImpl(other);
//            }
//            case INHERITED: {
//                if (bindings.inherited != null) {
//                    return bindings.inherited;
//                }
//                return convertImpl(other);
//            }
//            case FORMATTED: {
//                if (bindings.formatted != null) {
//                    return bindings.formatted;
//                }
//                return convertImpl(other);
//            }
//            case FILTERED: {
//                if (bindings.filtered != null) {
//                    return bindings.filtered;
//                }
//                return convertImpl(other);
//            }
//        }
//        throw new IllegalArgumentException("unsupported yet");
//    }
//
//    @Override
//    public OutputStream asOutputStream() {
//        if (osWrapper == null) {
//            osWrapper = new OutputStreamFromOutputStream(this);
//        }
//        return osWrapper;
//    }
//
//
//
//    @Override
//    public boolean isNtf() {
//        switch (getTerminalMode()) {
//            case FORMATTED:
//            case FILTERED: {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public NSystemTerminalBase getTerminal() {
//        return term;
//    }
//
//    public static class Bindings {
//        protected NOutputStreamBase raw;
//        protected NOutputStreamBase filtered;
//        protected NOutputStreamBase ansi;
//        protected NOutputStreamBase inherited;
//        protected NOutputStreamBase formatted;
//    }
//
//
//    @Override
//    public NFormat formatter() {
//        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "print-stream"));
//    }
//}
