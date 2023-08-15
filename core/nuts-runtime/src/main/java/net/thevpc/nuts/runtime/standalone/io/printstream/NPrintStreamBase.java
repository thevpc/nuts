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
    protected NSession session;
    protected Bindings bindings;
    protected OutputStream osWrapper;
    protected PrintStream psWrapper;
    protected Writer writerWrapper;
    protected boolean autoFlash;
    private NTerminalMode mode;
    protected NSystemTerminalBase term;
    private DefaultNContentMetadata md = new DefaultNContentMetadata();

    public NPrintStreamBase(boolean autoFlash, NTerminalMode mode, NSession session, Bindings bindings, NSystemTerminalBase term) {
        NAssert.requireNonNull(mode, "mode", session);
        this.bindings = bindings;
        this.autoFlash = autoFlash;
        this.mode = mode;
        this.session = session;
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

    public NSession getSession() {
        return session;
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
    public NPrintStream print(NString b) {
        if (b == null) {
            return printNull();
        }
        NText t = b.toText();
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
        this.print(txt().ofText(b));
        return this;
    }

    @Override
    public NPrintStream print(boolean b) {
        this.print(txt().ofText(b));
        return this;
    }

    @Override
    public NPrintStream print(Boolean b) {
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
    public NPrintStream print(char c) {
        this.print(String.valueOf(c));
        return this;
    }

    @Override
    public NPrintStream print(int i) {
        if (isNtf()) {
            this.print(String.valueOf(i));
        } else {
            this.print(txt().ofText(i));
        }
        return this;
    }

    @Override
    public NPrintStream print(long l) {
        this.print(txt().ofText(l));
        return this;
    }

    @Override
    public NPrintStream print(float f) {
        this.print(txt().ofText(f));
        return this;
    }

    @Override
    public NPrintStream print(double d) {
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NPrintStream print(Number d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NPrintStream print(Temporal d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
        return this;
    }

    @Override
    public NPrintStream print(Date d) {
        if (d == null) {
            return printNull();
        }
        this.print(txt().ofText(d));
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
    public NPrintStream println(NString b) {
        this.print(b);
        this.println();
        return this;
    }

    @Override
    public NPrintStream println(NMsg b) {
        this.println(txt().ofText(b));
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
        run(NTerminalCommand.CLEAR_LINE, session);
        run(NTerminalCommand.MOVE_LINE_START, session);
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
                print(txt.ofText(text));
            } else {
                print(txt.ofStyled(txt.ofText(text), styles));
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

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "print-stream"));
    }
}
