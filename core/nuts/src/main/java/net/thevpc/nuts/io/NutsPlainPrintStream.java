package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

public class NutsPlainPrintStream implements NutsPrintStream {
    private StringBuilder sb = new StringBuilder();
    private NutsSession session;
    private DefaultNutsOutputTargetMetadata md = new DefaultNutsOutputTargetMetadata();

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
    }

    @Override
    public NutsOutputTargetMetadata getOutputMetaData() {
        return md;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsPrintStream flush() {
        return this;
    }

    @Override
    public NutsPrintStream close() {
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] b) {
        sb.append(new String(b));
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        sb.append((char) b);
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf) {
        sb.append(buf);
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    @Override
    public NutsPrintStream print(NutsMessage b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NutsPrintStream print(NutsString b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NutsPrintStream print(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public NutsPrintStream print(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NutsPrintStream print(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public NutsPrintStream print(long l) {
        sb.append(l);
        return this;
    }

    @Override
    public NutsPrintStream print(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public NutsPrintStream print(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public NutsPrintStream print(char[] s) {
        sb.append(s);
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NutsPrintStream print(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NutsPrintStream printf(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NutsPrintStream printlnf(Object obj) {
        sb.append(obj);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println() {
        sb.append("\n");
        return this;
    }

    @Override
    public NutsPrintStream println(boolean x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(char x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(NutsMessage b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(NutsString b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(int x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(long x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(float x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(double x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(char[] x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(String x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream println(Object x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NutsPrintStream append(Object text, NutsTextStyle style) {
        sb.append(text);
        return this;
    }

    @Override
    public NutsPrintStream append(Object text, NutsTextStyles styles) {
        sb.append(text);
        return this;
    }

    @Override
    public NutsPrintStream resetLine() {
        return this;
    }

    @Override
    public NutsPrintStream printf(String format, Object... args) {
        print(NutsMessage.ofCstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printj(String format, Object... args) {
        print(NutsMessage.ofJstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printlnj(String format, Object... args) {
        println(NutsMessage.ofJstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printv(String format, Map<String, ?> args) {
        print(NutsMessage.ofVstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printlnv(String format, Map<String, ?> args) {
        println(NutsMessage.ofVstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printlnf(String format, Object... args) {
        println(NutsMessage.ofCstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream printf(Locale l, String format, Object... args) {
        print(NutsMessage.ofCstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream format(String format, Object... args) {
        print(NutsMessage.ofCstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream format(Locale l, String format, Object... args) {
        print(NutsMessage.ofCstyle(format, args));
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq) {
        print(csq);
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public NutsPrintStream append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return NutsTerminalMode.INHERITED;
    }

    @Override
    public boolean isAutoFlash() {
        return false;
    }

    @Override
    public NutsPrintStream setTerminalMode(NutsTerminalMode other) {
        return this;
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        return this;
    }

    @Override
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
    }

    @Override
    public PrintStream asPrintStream() {
        return new PrintStream(asOutputStream());
    }

    @Override
    public Writer asWriter() {
        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                sb.append(cbuf, off, len);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public boolean isNtf() {
        return false;
    }

    @Override
    public NutsSystemTerminalBase getTerminal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
