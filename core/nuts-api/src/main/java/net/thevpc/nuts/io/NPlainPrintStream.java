package net.thevpc.nuts.io;

import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public class NPlainPrintStream implements NPrintStream {
    private StringBuilder sb = new StringBuilder();
    private DefaultNContentMetadata md = new DefaultNContentMetadata();

    public NPlainPrintStream() {
    }

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b)  {
                sb.append((char) b);
            }
        };
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public NPrintStream flush() {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public NPrintStream print(byte[] b) {
        sb.append(new String(b));
        return this;
    }

    @Override
    public NPrintStream write(int b) {
        sb.append((char) b);
        return this;
    }

    @Override
    public NPrintStream print(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NPrintStream print(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream print(NMsg b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }

    @Override
    public NPrintStream print(NText b) {
        if (b != null) {
            sb.append(b);
        }
        return this;
    }


    @Override
    public NPrintStream print(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public NPrintStream print(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public NPrintStream print(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public NPrintStream print(long l) {
        sb.append(l);
        return this;
    }

    @Override
    public NPrintStream print(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public NPrintStream print(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public NPrintStream print(char[] s) {
        sb.append(s);
        return this;
    }

    @Override
    public NPrintStream print(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NPrintStream print(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public NPrintStream println() {
        sb.append("\n");
        return this;
    }

    @Override
    public NPrintStream println(boolean x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(char x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(NMsg b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NPrintStream println(NText b) {
        sb.append(b);
        println();
        return this;
    }

    @Override
    public NPrintStream println(int x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(long x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(float x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(double x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(char[] x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(String x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream println(Object x) {
        sb.append(x);
        println();
        return this;
    }

    @Override
    public NPrintStream print(Object text, NTextStyle style) {
        sb.append(text);
        return this;
    }

    @Override
    public NPrintStream print(Object text, NTextStyles styles) {
        sb.append(text);
        return this;
    }

    @Override
    public NPrintStream resetLine() {
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq) {
        sb.append(csq);
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq, int start, int end) {
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
    public NPrintStream setTerminalMode(NTerminalMode other) {
        return this;
    }

    @Override
    public NPrintStream run(NTerminalCmd command) {
        return this;
    }

    @Override
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b)  {
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
    public NPrintStream write(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

    @Override
    public NPrintStream write(char[] buf, int off, int len) {
        sb.append(buf, off, len);
        return this;
    }

    public NPrintStream printNull() {
        sb.append("null");
        return this;
    }

    @Override
    public NPrintStream print(Boolean b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream print(Number b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream print(Temporal b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream print(Date b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream println(Number b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream println(Temporal b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream println(Date b) {
        if (b == null) {
            return printNull();
        }
        return print(String.valueOf(b));
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        sb.append(new String(buf, off, len));
        return this;
    }

}
