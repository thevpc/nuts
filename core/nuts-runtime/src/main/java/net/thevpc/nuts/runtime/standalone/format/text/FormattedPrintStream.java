//package net.thevpc.nuts.runtime.standalone.util.fprint;
//
//import NutsTerminalMode;
//import NutsWorkspaceAware;
//import net.thevpc.nuts.runtime.standalone.io.NutsTerminalModeOp;
//
//import java.io.*;
//import java.util.Locale;
//
//import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
//
//public abstract class FormattedPrintStream extends PrintStreamExt implements NutsPrintStreamExt, NutsWorkspaceAware {
//
//    private FormatOutputStreamSupport formatNodeHelper = new FormatOutputStreamSupport();
//    private PrintStream ps;
//    private FormatOutputStreamSupport.RawOutputStream rawer = new FormatOutputStreamSupport.RawOutputStream() {
//        @Override
//        public void writeRaw(byte[] buf, int off, int len) {
//            writeRaw0(buf, off, len);
//        }
//    };
////    private DefaultNutsTextNodeParser partialParser = new DefaultNutsTextNodeParser();
//
//    public FormattedPrintStream(OutputStream out, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) {
//        super(out);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(OutputStream out, boolean autoFlush, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) {
//        super(out, autoFlush);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) throws UnsupportedEncodingException {
//        super(out, autoFlush, encoding);
//        init(renderer, parser);
//    }
//
//
//    //    public FormattedPrintStream(String fileName, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) throws FileNotFoundException {
////        super(fileName);
////        init(renderer, parser);
////    }
////
////    public FormattedPrintStream(String fileName, String csn, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) throws FileNotFoundException, UnsupportedEncodingException {
////        super(fileName, csn);
////        init(renderer, parser);
////    }
////
////    public FormattedPrintStream(File file, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) throws FileNotFoundException {
////        super(file);
////        init(renderer, parser);
////    }
////
////    public FormattedPrintStream(File file, String csn, FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) throws FileNotFoundException, UnsupportedEncodingException {
////        super(file, csn);
////        init(renderer, parser);
////    }
//    //////////////////////////////////////////
//    public FormattedPrintStream(OutputStream out, FormattedPrintStreamRenderer renderer) {
//        super(out);
//        init(renderer, null);
//    }
//
//    public FormattedPrintStream(OutputStream out, boolean autoFlush, FormattedPrintStreamRenderer renderer) {
//        super(out, autoFlush);
//        init(renderer, null);
//    }
//
//    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding, FormattedPrintStreamRenderer renderer) throws UnsupportedEncodingException {
//        super(out, autoFlush, encoding);
//        init(renderer, null);
//    }
//
//    //////////////////////////////////////////
////    public FormattedPrintStream(OutputStream out) {
////        super(out);
////        init(renderer, parser);
////    }
////
////    public FormattedPrintStream(OutputStream out, boolean autoFlush) {
////        super(out, autoFlush);
////        init(renderer, parser);
////    }
////
////    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
////        super(out, autoFlush, encoding);
////        init(renderer, parser);
////    }
//
//    //////////////////////////////////////////
//    private void init(FormattedPrintStreamRenderer renderer, NutsTextNodeParser parser) {
//        OutputStream base = getOut();
//        NutsTerminalModeOp t = CoreIOUtils.resolveNutsTerminalModeOp(base);
//        if (t.in() != NutsTerminalMode.INHERITED) {
//            throw new IllegalArgumentException("Illegal Formatted");
//        }
//
//        formatNodeHelper.setParser(parser);
//        formatNodeHelper.setRenderer(renderer);
//        formatNodeHelper.setRawer(rawer);
//    }
//
//    @Override
//    public NutsTerminalModeOp getModeOp() {
//        return NutsTerminalModeOp.FORMAT;
//    }
//
//
//    public PrintStream getUnformattedInstance() {
//        return CoreIOUtils.toPrintStream(getOut(),getWorkspace());
//    }
//
//
////    @Override
////    public void println(String text) {
////        print(text);
////        println();
////    }
//
//
//    @Override
//    public PrintStream append(char c) {
//        return super.append(c);
//    }
//
//    @Override
//    public PrintStream append(CharSequence csq, int start, int end) {
//        return super.append(csq, start, end);
//    }
//
//    @Override
//    public PrintStream append(CharSequence csq) {
//        return super.append(csq);
//    }
//
//    @Override
//    public PrintStream printf(Locale l, String format, Object... args) {
//        return super.printf(l, format, args);
//    }
//
//    @Override
//    public PrintStream printf(String format, Object... args) {
//        return super.printf(format, args);
//    }
//
//    @Override
//    public void println(Object x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(char[] x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(double x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(float x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(long x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(int x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(char x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println(boolean x) {
//        super.println(x);
//    }
//
//    @Override
//    public void println() {
//        super.println();
//    }
//
//    @Override
//    public void print(Object obj) {
//        super.print(obj);
//    }
//
//    @Override
//    public void print(char[] s) {
//        super.print(s);
//    }
//
//    @Override
//    public void print(double d) {
//        super.print(d);
//    }
//
//    @Override
//    public void print(float f) {
//        super.print(f);
//    }
//
//    @Override
//    public void print(long l) {
//        super.print(l);
//    }
//
//    @Override
//    public void print(int i) {
//        super.print(i);
//    }
//
//    @Override
//    public void print(char c) {
//        super.print(c);
//    }
//
//    @Override
//    public void print(boolean b) {
//        super.print(b);
//    }
//
//    public void writeRaw0(byte[] buf, int off, int len) {
//        super.write(buf, off, len);
//    }
//
//    @Override
//    public void write(byte[] buf, int off, int len) {
//        try {
//            formatNodeHelper.processBytes(buf, off, len);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//    }
//
//
//    @Override
//    public void write(int b) {
//        try {
//            formatNodeHelper.processByte(b);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//    }
//
//
//    @Override
//    public void flush() {
//        try {
//            formatNodeHelper.flush();
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//        super.flush();
//    }
//
//    @Override
//    public PrintStream basePrintStream() {
//        OutputStream b = baseOutputStream();
//        if (b instanceof PrintStream) {
//            return (PrintStream) b;
//        }
//        if (ps == null) {
//            ps = new PrintStream(b);
//        }
//        return ps;
//    }
//
//    @Override
//    public OutputStream baseOutputStream() {
//        return getOut();
//    }
//
//}
