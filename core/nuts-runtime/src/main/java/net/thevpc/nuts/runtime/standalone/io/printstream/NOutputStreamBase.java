package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.DefaultNOutputTargetMetadata;
import net.thevpc.nuts.io.NOutputTargetMetadata;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NAssert;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public abstract class NOutputStreamBase implements NOutputStream {
    private static String LINE_SEP = System.getProperty("line.separator");
    protected NSession session;
    protected Bindings bindings;
    protected OutputStream osWrapper;
    protected PrintStream psWrapper;
    protected Writer writerWrapper;
    protected boolean autoFlash;
    private NTerminalMode mode;
    private NSystemTerminalBase term;
    private DefaultNOutputTargetMetadata md = new DefaultNOutputTargetMetadata();

    public NOutputStreamBase(boolean autoFlash, NTerminalMode mode, NSession session, Bindings bindings, NSystemTerminalBase term) {
        NAssert.requireNonNull(mode, "mode", session);
        this.bindings = bindings;
        this.autoFlash = autoFlash;
        this.mode = mode;
        this.session = session;
        this.term = term;
    }

    public NOutputTargetMetadata getOutputMetaData() {
        return md;
    }

    protected abstract NOutputStream convertImpl(NTerminalMode other);

    @Override
    public String toString() {
        return super.toString();
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public NOutputStream print(byte[] b) {
        return print(b, 0, b.length);
    }

    private NOutputStream printNormalized(NText b) {
        if (b != null) {
            switch (b.getType()) {
                case LIST: {
                    for (NText child : ((NTextList) b).getChildren()) {
                        printNormalized(child);
                    }
                    break;
                }
                case PLAIN: {
                    print(b.toString());
                    break;
                }
                case STYLED: {
                    if (isNtf()) {
                        print(b.toString());
                    } else {
                        NTextStyled s = (NTextStyled) b;
                        printNormalized(s.getChild());
                    }
                    break;
                }
                case COMMAND: {
                    if (isNtf()) {
                        print(b.toString());
                    }
                    break;
                }
                case ANCHOR:
                case LINK:
                case CODE:
                case TITLE:
                default: {
                    if (session != null) {
                        throw new NUnsupportedOperationException(session);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NOutputStream print(NString b) {
        if (b == null) {
            return printNull();
        }
        NText t = b.toText();
        printNormalized(txt().transform(t,
                new NTextTransformConfig()
                        .setNormalize(true)
                        .setFlatten(true)
        ));
        return this;
    }

    @Override
    public NOutputStream print(NMsg b) {
        if (b == null) {
            return printNull();
        }
        this.print(txt().ofText(b));
        return this;
    }

    @Override
    public NOutputStream print(boolean b) {
        this.print(txt().ofText(b));
        return this;
    }

    @Override
    public NOutputStream print(Boolean b) {
        if (b == null) {
            return printNull();
        } else if (isNtf()) {
            this.print(txt().ofText(b));
        } else {
            this.print(String.valueOf(b));
        }
        return this;
    }

    protected NTexts txt() {
        return NTexts.of(session);
    }

    @Override
    public NOutputStream print(char c) {
        this.print(String.valueOf(c));
        return this;
    }

    @Override
    public NOutputStream print(int i) {
        if (isNtf()) {
            this.print(String.valueOf(i));
        } else {
            this.print(txt().ofText(i));
        }
        return this;
    }

    @Override
    public NOutputStream print(long l) {
        this.print(txt().ofText(l));
        return this;
    }

    @Override
    public NOutputStream print(float f) {
        this.print(txt().ofText(f));
        return this;
    }

    @Override
    public NOutputStream print(double d) {
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NOutputStream print(Number d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NOutputStream print(Temporal d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NOutputStream print(Date d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NOutputStream println(Number d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NOutputStream println(Temporal d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NOutputStream println(Date d) {
        print(d);
        println();
        return this;
    }

    @Override
    public NOutputStream print(char[] s) {
        if (s == null) {
            return printNull();
        }
        print(s, 0, s.length);
        return this;
    }

    @Override
    public NOutputStream print(Object obj) {
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
        } else if (obj instanceof NString) {
            this.print((NString) obj);
        } else if (obj instanceof char[]) {
            this.print((char[]) obj);
        } else if (obj instanceof byte[]) {
            this.print((byte[]) obj);
        } else {
            NObjectFormat.of(session).setValue(obj).print(this);
        }
        return this;
    }

    protected NOutputStream printNull() {
        return print("null");
    }

    @Override
    public NOutputStream println() {
        this.print(LINE_SEP);
        if (this.autoFlash) {
            flush();
        }
        return this;
    }

    @Override
    public NOutputStream println(boolean x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(char x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(NString b) {
        this.print(b);
        this.println();
        return this;
    }

    @Override
    public NOutputStream println(NMsg b) {
        this.println(txt().ofText(b));
        return this;
    }

    @Override
    public NOutputStream println(int x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(long x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(float x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(double x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(char[] x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(String x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(Object x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutputStream resetLine() {
        run(NTerminalCommand.CLEAR_LINE, session);
        run(NTerminalCommand.MOVE_LINE_START, session);
        return this;
    }

    @Override
    public NOutputStream print(CharSequence csq) {
        print(csq, 0, csq.length());
        return this;
    }

    @Override
    public NOutputStream print(CharSequence csq, int start, int end) {
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
    public NOutputStream setTerminalMode(NTerminalMode other) {
        if (other == null || other == this.getTerminalMode()) {
            return this;
        }
        switch (other) {
            case ANSI: {
                if (bindings.ansi != null) {
                    return bindings.filtered;
                }
                return convertImpl(other);
            }
            case INHERITED: {
                if (bindings.inherited != null) {
                    return bindings.inherited;
                }
                return convertImpl(other);
            }
            case FORMATTED: {
                if (bindings.formatted != null) {
                    return bindings.formatted;
                }
                return convertImpl(other);
            }
            case FILTERED: {
                if (bindings.filtered != null) {
                    return bindings.filtered;
                }
                return convertImpl(other);
            }
        }
        throw new IllegalArgumentException("unsupported yet");
    }

    @Override
    public OutputStream asOutputStream() {
        if (osWrapper == null) {
            osWrapper = new OutputStreamFromNOutputStream(this);
        }
        return osWrapper;
    }

    @Override
    public PrintStream asPrintStream() {
        if (psWrapper == null) {
            psWrapper = new PrintStreamFromNOutputStream((OutputStreamFromNOutputStream) asOutputStream());
        }
        return psWrapper;
    }

    @Override
    public Writer asWriter() {
        if (writerWrapper == null) {
            writerWrapper = new WriterFromNOutputStream(this);
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
        protected NOutputStreamBase raw;
        protected NOutputStreamBase filtered;
        protected NOutputStreamBase ansi;
        protected NOutputStreamBase inherited;
        protected NOutputStreamBase formatted;
    }

    @Override
    public NOutputStream print(Object text, NTextStyle style) {
        return print(text, NTextStyles.of(style));
    }

    @Override
    public NOutputStream print(Object text, NTextStyles styles) {
        if (text != null) {
            NTexts txt = txt();
            if (styles == null || styles.size() == 0) {
                print(txt.ofText(text));
            } else {
                print(txt.ofStyled(txt.ofText(text), styles));
            }
        }
        return this;
    }

    @Override
    public NOutputStream print(String s) {
        if (s == null) {
            return printNull();
        } else {
            char[] chars = s.toCharArray();
            write(chars, 0, chars.length);
        }
        return this;
    }

    @Override
    public NOutputStream print(byte[] buf, int off, int len) {
        return write(buf, off, len);
    }

    @Override
    public NOutputStream print(char[] buf, int off, int len) {
        return write(buf, off, len);
    }
}
