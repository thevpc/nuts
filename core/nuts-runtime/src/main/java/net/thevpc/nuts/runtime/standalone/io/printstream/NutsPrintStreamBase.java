package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;

public abstract class NutsPrintStreamBase implements NutsPrintStream {
    private static String LINE_SEP = System.getProperty("line.separator");
    //    private NutsWorkspace ws;
    protected NutsSession session;
    protected Bindings bindings;
    protected OutputStream osWrapper;
    protected PrintStream psWrapper;
    protected Writer writerWrapper;
    protected boolean autoFlash;
    private NutsTerminalMode mode;
    private boolean trouble = false;
    private NutsSystemTerminalBase term;

    public NutsPrintStreamBase(boolean autoFlash, NutsTerminalMode mode, NutsSession session, Bindings bindings,NutsSystemTerminalBase term) {
        if (mode == null) {
            throw new IllegalArgumentException("null mode");
        }
        if (session == null) {
            throw new IllegalArgumentException("null session");
        }
        this.bindings = bindings;
        this.autoFlash = autoFlash;
        this.mode = mode;
        this.session = session;
        this.term = term;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPrintStream write(byte[] b) {
        return write(b, 0, b.length);
    }

    @Override
    public NutsPrintStream write(char[] buf) {
        if (buf == null) {
            buf = "null".toCharArray();
        }
        return write(buf, 0, buf.length);
    }

    @Override
    public NutsPrintStream print(NutsString b) {
        this.print(String.valueOf(b));
        return this;
    }

    @Override
    public NutsPrintStream print(boolean b) {
        this.print(b ? "true" : "false");
        return this;
    }

    @Override
    public NutsPrintStream print(char c) {
        this.print(String.valueOf(c));
        return this;
    }

    @Override
    public NutsPrintStream print(int i) {
        this.print(String.valueOf(i));
        return this;
    }

    @Override
    public NutsPrintStream print(long l) {
        this.print(String.valueOf(l));
        return this;
    }

    @Override
    public NutsPrintStream print(float f) {
        this.print(String.valueOf(f));
        return this;
    }

    @Override
    public NutsPrintStream print(double d) {
        this.print(String.valueOf(d));
        return this;
    }

    @Override
    public NutsPrintStream print(char[] s) {
        if (s == null) {
            s = "null".toCharArray();
        }
        write(s, 0, s.length);
        return this;
    }

    @Override
    public NutsPrintStream print(Object obj) {
        this.print(String.valueOf(obj));
        return this;
    }

    @Override
    public NutsPrintStream printf(Object obj) {
        NutsObjectFormat.of(session).setValue(obj).print(this);
        return this;
    }

    @Override
    public NutsPrintStream printlnf(Object obj) {
        NutsObjectFormat.of(session).setValue(obj).println(this);
        return this;
    }

    @Override
    public NutsPrintStream println() {
        this.print(LINE_SEP);
        if (this.autoFlash) {
            flush();
        }
        return this;
    }

    @Override
    public NutsPrintStream println(boolean x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(char x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(NutsString b) {
        this.println(String.valueOf(b));
        return this;
    }

    @Override
    public NutsPrintStream println(int x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(long x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(float x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(double x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(char[] x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(String x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(Object x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream resetLine() {
        run(NutsTerminalCommand.CLEAR_LINE, session);
        run(NutsTerminalCommand.MOVE_LINE_START, session);
        return this;
    }

    @Override
    public NutsPrintStream printf(String format, Object... args) {
        format(format, args);
        return this;
    }

    @Override
    public NutsPrintStream printj(String format, Object... args) {
        NutsText s = NutsTexts.of(session).toText(
                NutsMessage.jstyle(
                        format, args
                )
        );
        print(s);
        return this;
    }

    @Override
    public NutsPrintStream printlnf(String format, Object... args) {
        format(format, args);
        println();
        return this;
    }

    @Override
    public NutsPrintStream printf(Locale l, String format, Object... args) {
        format(l, format, args);
        return this;
    }

    @Override
    public NutsPrintStream format(String format, Object... args) {
        return format(null, format, args);
    }

    @Override
    public NutsPrintStream format(Locale l, String format, Object... args) {
        if (l == null) {
            NutsText s = NutsTexts.of(session).toText(
                    NutsMessage.cstyle(
                            format, args
                    )
            );
            print(s);
        } else {
            NutsSession sess = this.session.copy().setLocale(l.toString());
            NutsText s = NutsTexts.of(sess).toText(
                    NutsMessage.cstyle(
                            format, args
                    )
            );
            print(s);
        }
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq) {
        append(csq, 0, csq.length());
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq, int start, int end) {
        int bufferLength = Math.min(4096, (end - start));
        char[] buffer = new char[bufferLength];
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
    public NutsPrintStream append(char c) {
        print(c);
        return this;
    }

    @Override
    public NutsTerminalMode mode() {
        return mode;
    }

    @Override
    public boolean isAutoFlash() {
        return autoFlash;
    }

    @Override
    public NutsPrintStream setMode(NutsTerminalMode other) {
        if (other == null || other == this.mode()) {
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
            osWrapper = new OutputStreamFromNutsPrintStream(this);
        }
        return osWrapper;
    }

    @Override
    public PrintStream asPrintStream() {
        if (psWrapper == null) {
            psWrapper = new PrintStreamFromNutsPrintStream((OutputStreamFromNutsPrintStream) asOutputStream());
        }
        return psWrapper;
    }

    @Override
    public Writer asWriter() {
        if (writerWrapper == null) {
            writerWrapper = new WriterFromNutsPrintStream(this);
        }
        return writerWrapper;
    }

    @Override
    public boolean isNtf() {
        switch (mode()) {
            case FORMATTED:
            case FILTERED: {
                return true;
            }
        }
        return false;
    }

    protected abstract NutsPrintStream convertImpl(NutsTerminalMode other);

    @Override
    public String toString() {
        return super.toString();
    }

    public static class Bindings {
        protected NutsPrintStreamBase raw;
        protected NutsPrintStreamBase filtered;
        protected NutsPrintStreamBase ansi;
        protected NutsPrintStreamBase inherited;
        protected NutsPrintStreamBase formatted;
    }

    public NutsSystemTerminalBase getTerminal() {
        return term;
    }
}
