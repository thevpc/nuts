//package net.thevpc.nuts.runtime.util.fprint;
//
//import net.thevpc.nuts.core.io.NutsFormattedPrintStream;
//import NutsOutputStreamTransparentAdapter;
//import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;
//
//import java.io.OutputStream;
//import NutsSupportLevelContext;
//import NutsTerminalMode;
//import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
//
///**
// * Created by vpc on 2/20/17.
// */
//public class NutsPrintStreamFormattedUnixAnsi extends FormattedPrintStream implements NutsFormattedPrintStream, NutsOutputStreamTransparentAdapter {
//
//    private OutputStream out;
//
//    public NutsPrintStreamFormattedUnixAnsi(OutputStream out) {
//        super(out, FPrint.RENDERER_ANSI);
//        this.out = out;
//        NutsTerminalModeOp t = CoreIOUtils.resolveNutsTerminalModeOp(out);
//        if(t.in()!=NutsTerminalMode.INHERITED){
//            throw new IllegalArgumentException("Illegal Formatted");
//        }
//    }
//
//    @Override
//    public NutsTerminalModeOp getModeOp() {
//        return NutsTerminalModeOp.FORMAT;
//    }
//
//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }
//
//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<OutputStream> criteria) {
//        return DEFAULT_SUPPORT + 2;
//    }
//
//    public NutsPrintStreamExt filter(){
//        return new NutsPrintStreamFiltered(this);
//    }
//
//}
