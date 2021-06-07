//package net.thevpc.nuts.runtime.core.format.text;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
//import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;
//
//import java.io.*;
//import java.util.Locale;
//
//public class PrintStreamExt extends NutsPrintStreamBase {
//    private OutputStream out;
//    private PrintStream base;
//
//    public PrintStreamExt(OutputStream out,NutsTerminalMode mode,NutsSession session) {
//        this.out = out;
//        this.base = new PrintStream(out);
//        this.autoFlash2 = false;
//        this.mode = mode;
//        this.session = session;
//    }
//
//    public PrintStreamExt(OutputStream out, boolean autoFlush,NutsTerminalMode mode,NutsSession session) {
//        this.out = out;
//        this.base = new PrintStream(out,autoFlush);
//        this.autoFlash2 = autoFlush;
//    }
//
//    public PrintStreamExt(OutputStream out, boolean autoFlush, String encoding,NutsTerminalMode mode,NutsSession session) throws UnsupportedEncodingException {
//        this.out = out;
//        this.base = new PrintStream(out,autoFlush,encoding);
//        this.autoFlash2 = autoFlush;
//        this.mode = mode;
//    }
//
//    public PrintStreamExt(String fileName,NutsTerminalMode mode,NutsSession session) throws FileNotFoundException {
//        this(new FileOutputStream(fileName), false,mode,session);
//        this.autoFlash2 = false;
//    }
//
//    public PrintStreamExt(String fileName, String csn,NutsTerminalMode mode,NutsSession session) throws FileNotFoundException, UnsupportedEncodingException {
//        this(new FileOutputStream(fileName), false, csn,mode,session);
//        this.autoFlash2 = false;
//    }
//
//    public PrintStreamExt(File file,NutsTerminalMode mode,NutsSession session) throws FileNotFoundException {
//        this(new FileOutputStream(file), false,mode,session);
//        this.autoFlash2 = false;
//    }
//
//    public PrintStreamExt(File file, String csn,NutsTerminalMode mode,NutsSession session) throws FileNotFoundException, UnsupportedEncodingException {
//        this(new FileOutputStream(file), false, csn,mode,session);
//        this.autoFlash2 = false;
//    }
//
//
//    public boolean isAutoFlash() {
//        return autoFlash2;
//    }
//
//    public OutputStream getOut() {
//        return out;
//    }
//
//    @Override
//    public NutsTerminalModeOp getModeOp() {
//        OutputStream base = getOut();
//        if (base instanceof ExtendedFormatAware) {
//            return ((ExtendedFormatAware) base).getModeOp();
//        }
//        return NutsTerminalModeOp.NOP;
//    }
//
//    @Override
//    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
//        if (other == null || other == getModeOp()) {
//            return this;
//        }
//        if(out instanceof ExtendedFormatAware){
//            return ((ExtendedFormatAware) out).convert(other);
//        }
//        return new RawOutputStream(out,session).convert(other);
//    }
//
//    @Override
//    public void setSession(NutsSession session) {
//        this.session=session;
////        this.ws=session==null?null:session.getWorkspace();
//    }
//
//    @Override
//    public NutsPrintStream format(String format, Object... args) {
//        return format(null,format,args);
//    }
//
//    @Override
//    public NutsPrintStream format(Locale l, String format, Object... args) {
//        if(l==null){
//            NutsText s = session.getWorkspace().text().setSession(session).toText(
//                    NutsMessage.cstyle(
//                            format, args
//                    )
//            );
//            print(s);
//        }else{
//            NutsSession sess = this.session.copy().setLocale(l.toString());
//            NutsText s = sess.getWorkspace().text().setSession(sess).toText(
//                    NutsMessage.cstyle(
//                            format, args
//                    )
//            );
//            print(s);
//        }
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream flush() {
//        base.flush();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream close() {
//        base.close();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream write(byte[] b) {
//        try {
//            base.write(b);
//        } catch (IOException e) {
//            //just ignore
//            trouble = true;
//        }
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream write(int b) {
//        base.write(b);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream write(byte[] buf, int off, int len) {
//        base.write(buf, off, len);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(boolean b) {
//        this.print(b ? "true" : "false");
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(char c) {
//        this.print(String.valueOf(c));
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(int i) {
//        this.print(String.valueOf(i));
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(long l) {
//        this.print(String.valueOf(l));
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(float f) {
//        this.print(String.valueOf(f));
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(double d) {
//        this.print(String.valueOf(d));
//        return this;
//    }
//
//
//    @Override
//    public NutsPrintStream print(Object obj) {
//        this.print(String.valueOf(obj));
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println() {
//        this.print(LINE_SEP);
//        if (this.autoFlash2) {
//            flush();
//        }
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(boolean x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(char x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(int x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(long x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(float x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(double x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(char[] x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(String x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream println(Object x) {
//        print(x);
//        println();
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream printf(String format, Object... args) {
//        base.printf(format, args);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream printf(Locale l, String format, Object... args) {
//        base.printf(l, format, args);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream append(CharSequence csq) {
//        base.append(csq);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream append(CharSequence csq, int start, int end) {
//        base.append(csq, start, end);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream append(char c) {
//        base.append(c);
//        return this;
//    }
//
//
//    protected void baseWriteChars(char[] chars){
//        switch (mode){
//            case FILTERED:{
//                try {
//                    //ESCAPE
//                    base.write(
//                            DefaultNutsTextNodeParser.escapeText0(new String(chars)).getBytes()
//                    );
//                } catch (IOException e) {
//                    trouble=true;
//                }
//                break;
//            }
//        }
//    }
//
//    @Override
//    public NutsPrintStream print(char[] s) {
//        base.print(s);
//        return this;
//    }
//
//    @Override
//    public NutsPrintStream print(String s) {
//        base.print(s);
//        return this;
//    }
//}
