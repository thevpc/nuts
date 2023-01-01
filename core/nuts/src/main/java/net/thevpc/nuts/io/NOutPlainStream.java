package net.thevpc.nuts.io;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NString;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

public class NOutPlainStream implements NOutStream {
    private StringBuilder sb = new StringBuilder();
    private NSession session;
    private DefaultNOutputTargetMetadata md = new DefaultNOutputTargetMetadata();

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
    public NOutputTargetMetadata getOutputMetaData() {
        return md;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NOutStream setSession(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NOutStream flush() {
        return this;
    }

    @Override
    public NOutStream close() {
        return this;
    }

    @Override
    public NOutStream write(byte[] b) {
        sb.append(new String(b));
        return this;
    }

    @Override
    public NOutStream write(int b) {
        sb.append((char) b);
        return this;
    }

    @Override
    public NOutStream write(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NOutStream write(char[] buf) {
        sb.append(buf);
        return this;
    }

    @Override
    public NOutStream write(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    @Override
    public NOutStream print(NMsg b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NOutStream print(NString b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NOutStream print(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public NOutStream print(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NOutStream print(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public NOutStream print(long l) {
        sb.append(l);
        return this;
    }

    @Override
    public NOutStream print(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public NOutStream print(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public NOutStream print(char[] s) {
        sb.append(s);
        return this;
    }

    @Override
    public NOutStream print(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NOutStream print(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NOutStream printf(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NOutStream printlnf(Object obj) {
        sb.append(obj);
        println();
        return this;
    }

    @Override
    public NOutStream println() {
        sb.append("\n");
        return this;
    }

    @Override
    public NOutStream println(boolean x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(char x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(NMsg b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NOutStream println(NString b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NOutStream println(int x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(long x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(float x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(double x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(char[] x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(String x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(Object x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutStream append(Object text, NTextStyle style) {
        sb.append(text);
        return this;
    }

    @Override
    public NOutStream append(Object text, NTextStyles styles) {
        sb.append(text);
        return this;
    }

    @Override
    public NOutStream resetLine() {
        return this;
    }

    @Override
    public NOutStream printf(String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printj(String format, Object... args) {
        print(NMsg.ofJstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printlnj(String format, Object... args) {
        println(NMsg.ofJstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printv(String format, Map<String, ?> args) {
        print(NMsg.ofVstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printlnv(String format, Map<String, ?> args) {
        println(NMsg.ofVstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printlnf(String format, Object... args) {
        println(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NOutStream printf(Locale l, String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NOutStream format(String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NOutStream format(Locale l, String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NOutStream append(CharSequence csq) {
        print(csq);
        return this;
    }

    @Override
    public NOutStream append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public NOutStream append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NTerminalMode getTerminalMode() {
        return NTerminalMode.INHERITED;
    }

    @Override
    public boolean isAutoFlash() {
        return false;
    }

    @Override
    public NOutStream setTerminalMode(NTerminalMode other) {
        return this;
    }

    @Override
    public NOutStream run(NTerminalCommand command, NSession session) {
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
    public NSystemTerminalBase getTerminal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
