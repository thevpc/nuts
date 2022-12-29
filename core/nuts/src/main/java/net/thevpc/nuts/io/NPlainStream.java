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

public class NPlainStream implements NStream {
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
    public NStream setSession(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NStream flush() {
        return this;
    }

    @Override
    public NStream close() {
        return this;
    }

    @Override
    public NStream write(byte[] b) {
        sb.append(new String(b));
        return this;
    }

    @Override
    public NStream write(int b) {
        sb.append((char) b);
        return this;
    }

    @Override
    public NStream write(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NStream write(char[] buf) {
        sb.append(buf);
        return this;
    }

    @Override
    public NStream write(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    @Override
    public NStream print(NMsg b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NStream print(NString b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NStream print(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public NStream print(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NStream print(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public NStream print(long l) {
        sb.append(l);
        return this;
    }

    @Override
    public NStream print(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public NStream print(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public NStream print(char[] s) {
        sb.append(s);
        return this;
    }

    @Override
    public NStream print(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NStream print(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NStream printf(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NStream printlnf(Object obj) {
        sb.append(obj);
        println();
        return this;
    }

    @Override
    public NStream println() {
        sb.append("\n");
        return this;
    }

    @Override
    public NStream println(boolean x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(char x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(NMsg b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NStream println(NString b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NStream println(int x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(long x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(float x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(double x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(char[] x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(String x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream println(Object x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NStream append(Object text, NTextStyle style) {
        sb.append(text);
        return this;
    }

    @Override
    public NStream append(Object text, NTextStyles styles) {
        sb.append(text);
        return this;
    }

    @Override
    public NStream resetLine() {
        return this;
    }

    @Override
    public NStream printf(String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NStream printj(String format, Object... args) {
        print(NMsg.ofJstyle(format, args));
        return this;
    }

    @Override
    public NStream printlnj(String format, Object... args) {
        println(NMsg.ofJstyle(format, args));
        return this;
    }

    @Override
    public NStream printv(String format, Map<String, ?> args) {
        print(NMsg.ofVstyle(format, args));
        return this;
    }

    @Override
    public NStream printlnv(String format, Map<String, ?> args) {
        println(NMsg.ofVstyle(format, args));
        return this;
    }

    @Override
    public NStream printlnf(String format, Object... args) {
        println(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NStream printf(Locale l, String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NStream format(String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NStream format(Locale l, String format, Object... args) {
        print(NMsg.ofCstyle(format, args));
        return this;
    }

    @Override
    public NStream append(CharSequence csq) {
        print(csq);
        return this;
    }

    @Override
    public NStream append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public NStream append(char c) {
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
    public NStream setTerminalMode(NTerminalMode other) {
        return this;
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
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
