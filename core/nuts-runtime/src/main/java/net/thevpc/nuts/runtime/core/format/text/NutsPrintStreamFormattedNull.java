//package net.thevpc.nuts.runtime.standalone.util.fprint;
//
//import net.thevpc.nuts.core.io.NutsFormattedPrintStream;
//import NutsOutputStreamTransparentAdapter;
//
//import java.io.*;
//import NutsSupportLevelContext;
//import net.thevpc.nuts.runtime.standalone.util.io.NullOutputStream;
//
///**
// * Created by vpc on 2/20/17.
// */
//public class NutsPrintStreamFormattedNull extends FormattedPrintStream implements NutsFormattedPrintStream, NutsOutputStreamTransparentAdapter {
//
//    public NutsPrintStreamFormattedNull() {
//        super(NullOutputStream.INSTANCE, FPrint.RENDERER_ANSI_STRIPPER);
//    }
//
//    public NutsPrintStreamFormattedNull(OutputStream out) {
//        super(out, FPrint.RENDERER_ANSI_STRIPPER);
//    }
//
//    public NutsPrintStreamFormattedNull(OutputStream out, boolean autoFlush) {
//        super(out, autoFlush, FPrint.RENDERER_ANSI_STRIPPER);
//    }
//
//    public NutsPrintStreamFormattedNull(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
//        super(out, autoFlush, encoding, FPrint.RENDERER_ANSI_STRIPPER);
//    }
//
//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<OutputStream> criteria) {
//        return DEFAULT_SUPPORT + 1;
//    }
//
//    public NutsPrintStreamExt filter() {
//        return new NutsPrintStreamFiltered(this);
//    }
//}
