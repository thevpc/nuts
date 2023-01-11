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
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class NPlainOutputStream implements NOutputStream {
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
    public NOutputStream setSession(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NOutputStream flush() {
        return this;
    }

    @Override
    public NOutputStream close() {
        return this;
    }

    @Override
    public NOutputStream print(byte[] b) {
        sb.append(new String(b));
        return this;
    }

    @Override
    public NOutputStream write(int b) {
        sb.append((char) b);
        return this;
    }

    @Override
    public NOutputStream print(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NOutputStream print(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    @Override
    public NOutputStream print(NMsg b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NOutputStream print(NString b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }


    @Override
    public NOutputStream print(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public NOutputStream print(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NOutputStream print(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public NOutputStream print(long l) {
        sb.append(l);
        return this;
    }

    @Override
    public NOutputStream print(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public NOutputStream print(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public NOutputStream print(char[] s) {
        sb.append(s);
        return this;
    }

    @Override
    public NOutputStream print(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NOutputStream print(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NOutputStream println() {
        sb.append("\n");
        return this;
    }

    @Override
    public NOutputStream println(boolean x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(char x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(NMsg b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NOutputStream println(NString b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NOutputStream println(int x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(long x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(float x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(double x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(char[] x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(String x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream println(Object x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NOutputStream print(Object text, NTextStyle style) {
        sb.append(text);
        return this;
    }

    @Override
    public NOutputStream print(Object text, NTextStyles styles) {
        sb.append(text);
        return this;
    }

    @Override
    public NOutputStream resetLine() {
        return this;
    }

    @Override
    public NOutputStream print(CharSequence csq) {
        sb.append(csq);
        return this;
    }

    @Override
    public NOutputStream print(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
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
    public NOutputStream setTerminalMode(NTerminalMode other) {
        return this;
    }

    @Override
    public NOutputStream run(NTerminalCommand command, NSession session) {
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

    @Override
    public NOutputStream write(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NOutputStream write(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    public NOutputStream printNull() {
        sb.append("null");
        return this;
    }

    @Override
    public NOutputStream print(Boolean b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream print(Number b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream print(Temporal b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream print(Date b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream println(Number b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream println(Temporal b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NOutputStream println(Date b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }
}
