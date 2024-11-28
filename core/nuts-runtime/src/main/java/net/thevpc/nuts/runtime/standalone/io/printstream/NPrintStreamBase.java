package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public abstract class NPrintStreamBase implements NPrintStream {
    private static String LINE_SEP = System.getProperty("line.separator");
    protected NWorkspace workspace;
    protected Bindings bindings;
    protected OutputStream osWrapper;
    protected PrintStream psWrapper;
    protected Writer writerWrapper;
    protected boolean autoFlash;
    private NTerminalMode mode;
    protected NSystemTerminalBase term;
    private DefaultNContentMetadata md = new DefaultNContentMetadata();

    public NPrintStreamBase(boolean autoFlash, NTerminalMode mode, NWorkspace workspace, Bindings bindings, NSystemTerminalBase term) {
        NAssert.requireNonNull(mode, "mode");
        bindings.setOrErr(this, mode);
        this.bindings = bindings;
        this.autoFlash = autoFlash;
        this.mode = mode;
        this.workspace = workspace;
        if(term==null && mode==NTerminalMode.ANSI){
            term=new AnsiNPrintStreamTerminalBase(workspace,this);
        }
        this.term = term;
    }

    public NContentMetadata getMetaData() {
        return md;
    }

    protected abstract NPrintStream convertImpl(NTerminalMode other);

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public NPrintStream print(byte[] b) {
        return print(b, 0, b.length);
    }

    protected NPrintStream printParsed(NText b) {
        print(b.toString());
        return this;
    }

    private NPrintStream printNormalized(NText b) {
        if (b != null) {
            switch (b.getType()) {
                case LIST: {
                    for (NText child : ((NTextList) b).getChildren()) {
                        printNormalized(child);
                    }
                    break;
                }
                case BUILDER: {
                    for (NText child : ((NTextBuilder) b).getChildren()) {
                        printNormalized(child);
                    }
                    break;
                }
                case PLAIN: {
                    printParsed(b);
                    break;
                }
                case STYLED: {
                    if (isNtf()) {
                        printParsed(b);
                    } else {
                        NTextStyled s = (NTextStyled) b;
                        printNormalized(s.getChild());
                    }
                    break;
                }
                case COMMAND: {
                    if (isNtf()) {
                        printParsed(b);
                    }
                    break;
                }
                case ANCHOR:
                case LINK:
                case CODE:
                case TITLE:
                default: {
                    if (workspace != null) {
                        throw new NUnsupportedOperationException();
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NPrintStream print(NText b) {
        if (b == null) {
            return printNull();
        }
        NText t = txt().of(b);
        NText transformed = txt().transform(t,
                new NTextTransformConfig()
                        .setNormalize(true)
                        .setFlatten(true)
        );
        printNormalized(transformed);
        return this;
    }

    @Override
    public NPrintStream print(NMsg b) {
        if (b == null) {
            return printNull();
        }
        this.print(txt().of(b));
        return this;
    }

    @Override
    public NPrintStream print(boolean b) {
        this.print(txt().of(b));
        return this;
    }

    @Override
    public NPrintStream print(Boolean b) {
        if (b == null) {
            return printNull();
        } else if (isNtf()) {
            this.print(txt().of(b));
        } else {
            this.print(String.valueOf(b));
        }
        return this;
    }

    protected NTexts txt() {
        return NTexts.of();
    }

    @Override
    public NPrintStream print(char c) {
        this.print(String.valueOf(c));
        return this;
    }

    @Override
    public NPrintStream print(int i) {
        if (isNtf()) {
            this.print(String.valueOf(i));
        } else {
            this.print(txt().of(i));
        }
        return this;
    }

    @Override
    public NPrintStream print(long l) {
        this.print(txt().of(l));
        return this;
    }

    @Override
    public NPrintStream print(float f) {
        this.print(txt().of(f));
        return this;
    }

    @Override
    public NPrintStream print(double d) {
        this.print(txt().of(d));
        return this;
    }

    @Override
    public NPrintStream print(Number d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().of(d));
        return this;
    }

    @Override
    public NPrintStream print(Temporal d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().of(d));
        return this;
    }

    @Override
    public NPrintStream print(Date d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().of(d));
        return this;
    }

    @Override
    public NPrintStream println(Number d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NPrintStream println(Temporal d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NPrintStream println(Date d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NPrintStream print(char[] s) {
        if (s == null) {
            return printNull();
        }
        print(s, 0, s.length);
        return this;
    }

    @Override
    public NPrintStream print(Object obj) {
        if (obj == null) {
            return this.printNull();
        } else if (obj instanceof CharSequence) {
            this.print((CharSequence) obj);
        } else if (obj instanceof Number) {
            this.print((Number) obj);
        } else if (obj instanceof Date) {
            this.print((Date) obj);
        } else if (obj instanceof Temporal) {
            this.print((Temporal) obj);
        } else if (obj instanceof NMsg) {
            this.print((NMsg) obj);
        } else if (obj instanceof NText) {
            this.print((NText) obj);
        } else if (obj instanceof char[]) {
            this.print((char[]) obj);
        } else if (obj instanceof byte[]) {
            this.print((byte[]) obj);
        } else {
            NObjectFormat.of().setValue(obj).print(this);
        }
        return this;
    }

    protected NPrintStream printNull() {
        return print("null");
    }

    @Override
    public NPrintStream println() {
        this.print(LINE_SEP);
        if (this.autoFlash) {
            flush();
        }
        return this;
    }

    @Override
    public NPrintStream println(boolean x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(char x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(NText b) {
        this.print(b);
        this.println();
        return this;
    }

    @Override
    public NPrintStream println(NMsg b) {
        this.println(txt().of(b));
        return this;
    }

    @Override
    public NPrintStream println(int x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(long x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(float x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(double x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(char[] x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(String x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(Object x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NPrintStream resetLine() {
        run(NTerminalCmd.CLEAR_LINE);
        run(NTerminalCmd.MOVE_LINE_START);
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq) {
        print(csq, 0, csq.length());
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq, int start, int end) {
        int bufferLength = Math.min(4096, (end - start));
        int i = start;
        while (i < end) {
            int e = Math.min(i + bufferLength, end);
            String s = csq.subSequence(i, e).toString();
            print(s);
            i += bufferLength;
        }
        return this;
    }

    @Override
    public NTerminalMode getTerminalMode() {
        return mode;
    }

    @Override
    public boolean isAutoFlash() {
        return autoFlash;
    }

    @Override
    public NPrintStream setTerminalMode(NTerminalMode other) {
        if (other == null || other == this.getTerminalMode()) {
            return this;
        }
        NPrintStreamBase o = bindings.get(other);
        if (o != null) {
            return o;
        }
        return convertImpl(other);
    }

    @Override
    public OutputStream asOutputStream() {
        if (osWrapper == null) {
            osWrapper = new OutputStreamFromNPrintStream(this);
        }
        return osWrapper;
    }

    @Override
    public PrintStream asPrintStream() {
        if (psWrapper == null) {
            psWrapper = new PrintStreamFromNPrintStream((OutputStreamFromNPrintStream) asOutputStream());
        }
        return psWrapper;
    }

    @Override
    public Writer asWriter() {
        if (writerWrapper == null) {
            writerWrapper = new WriterFromNPrintStream(this);
        }
        return writerWrapper;
    }

    @Override
    public boolean isNtf() {
        switch (getTerminalMode()) {
            case FORMATTED:
            case FILTERED: {
                return true;
            }
        }
        return false;
    }

    public NSystemTerminalBase getTerminal() {
        return term;
    }

    public static class Bindings {
        protected NPrintStreamBase raw;
        protected NPrintStreamBase filtered;
        protected NPrintStreamBase ansi;
        protected NPrintStreamBase inherited;
        protected NPrintStreamBase formatted;

        public void set(NPrintStreamBase o, NTerminalMode mode) {
            NAssert.requireNonNull(o, "terminal");
            NAssert.requireNonNull(mode, "mode");
            switch (mode) {
                case ANSI: {
                    this.ansi = o;
                    if (this.raw == null) {
                        this.raw = this.ansi;
                    }
                    break;
                }
                case FILTERED: {
                    this.filtered = o;
                    break;
                }
                case FORMATTED: {
                    this.formatted = o;
                    break;
                }
                case INHERITED: {
                    this.inherited = o;
                    if (this.raw == null) {
                        this.raw = this.ansi;
                    }
                    break;
                }
                case DEFAULT: {
                    this.raw = o;
                    break;
                }
            }
        }

        public NPrintStreamBase get(NTerminalMode mode) {
            NAssert.requireNonNull(mode, "mode");
            switch (mode) {
                case FILTERED:
                    return this.filtered;
                case ANSI:
                    return this.ansi;
                case FORMATTED:
                    return this.formatted;
                case INHERITED:
                    return this.inherited;
                case DEFAULT:
                    return this.raw;
            }
            throw new IllegalArgumentException("unexpected");
        }

        public void setOrErr(NPrintStreamBase o, NTerminalMode mode) {
            setIfNull(o,mode,true);
        }

        public void setIfNull(NPrintStreamBase o, NTerminalMode mode, boolean err) {
            NAssert.requireNonNull(o, "terminal");
            NAssert.requireNonNull(mode, "mode");
            switch (mode) {
                case ANSI: {
                    if (this.ansi == null) {
                        this.ansi = o;
                        if (this.raw == null) {
                            this.raw = this.ansi;
                        }
                    } else {
                        if (err) {
                            throw new IllegalArgumentException("already bound " + mode);
                        }
                    }
                    break;
                }
                case FILTERED: {
                    if (this.filtered == null) {
                        this.filtered = o;
                    } else {
                        if (err) {
                            throw new IllegalArgumentException("already bound " + mode);
                        }
                    }
                    break;
                }
                case FORMATTED: {
                    if (this.formatted == null) {
                        this.formatted = o;
                    } else {
                        if (err) {
                            throw new IllegalArgumentException("already bound " + mode);
                        }
                    }
                    break;
                }
                case INHERITED: {
                    if (this.inherited == null) {
                        this.inherited = o;
                        if (this.ansi == null) {
                            this.ansi = o;
                        }
                        if (this.raw == null) {
                            this.raw = this.ansi;
                        }
                    } else {
                        if (err) {
                            throw new IllegalArgumentException("already bound " + mode);
                        }
                    }
                    break;
                }
                case DEFAULT: {
                    if (this.raw == null) {
                        this.raw = o;
                    }else {
                        if (err) {
                            throw new IllegalArgumentException("already bound " + mode);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public NPrintStream print(Object text, NTextStyle style) {
        return print(text, NTextStyles.of(style));
    }

    @Override
    public NPrintStream print(Object text, NTextStyles styles) {
        if (text != null) {
            NTexts txt = txt();
            if (styles == null || styles.size() == 0) {
                print(txt.of(text));
            } else {
                print(txt.ofStyled(txt.of(text), styles));
            }
        }
        return this;
    }

    @Override
    public NPrintStream print(String s) {
        if (s == null) {
            return printNull();
        } else {
            char[] chars = s.toCharArray();
            write(chars, 0, chars.length);
        }
        return this;
    }

    @Override
    public NPrintStream print(byte[] buf, int off, int len) {
        return write(buf, off, len);
    }

    @Override
    public NPrintStream print(char[] buf, int off, int len) {
        return write(buf, off, len);
    }

}
