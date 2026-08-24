package net.thevpc.nuts.io;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NTrace class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NTrace {
    /**
     * Flush.
     *
     * @return flush result
     */
    public static NPrintStream flush() {
        /**
         * Out.
         *
         * @param ).flush( ).flush(
         * @return out result
         */
        return out().flush();
    }


    /**
     * Close.
     *
     * @return close result
     */
    public static NPrintStream close() {
      /**
       * Out.
       *
       * @param ).close( ).close(
       */
        out().close();
        /**
         * Out.
         *
         * @return out result
         */
        return out();
    }

    /**
     * Write raw.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write raw result
     */
    public static NPrintStream writeRaw(byte[] buf, int off, int len) {
        /**
         * Out.
         *
         * @param ).writeRaw(buf ).write raw(buf
         * @param off off
         * @param len len
         * @return out result
         */
        return out().writeRaw(buf, off, len);
    }

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    public static NPrintStream write(byte[] buf, int off, int len) {
        /**
         * Out.
         *
         * @param ).write(buf ).write(buf
         * @param off off
         * @param len len
         * @return out result
         */
        return out().write(buf, off, len);
    }

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    public static NPrintStream write(char[] buf, int off, int len) {
        /**
         * Out.
         *
         * @param ).write(buf ).write(buf
         * @param off off
         * @param len len
         * @return out result
         */
        return out().write(buf, off, len);
    }

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    public static NPrintStream print(byte[] buf, int off, int len) {
        /**
         * Out.
         *
         * @param ).print(buf ).print(buf
         * @param off off
         * @param len len
         * @return out result
         */
        return out().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    public static NPrintStream print(char[] buf, int off, int len) {
        /**
         * Out.
         *
         * @param ).print(buf ).print(buf
         * @param off off
         * @param len len
         * @return out result
         */
        return out().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(byte[] b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Write.
     *
     * @param b b
     * @return write result
     */
    public static NPrintStream write(int b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NMsg b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NText b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(Boolean b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(boolean b) {
        /**
         * Out.
         *
         * @param ).print(b ).print(b
         * @return out result
         */
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param c c
     * @return print result
     */
    public static NPrintStream print(char c) {
        /**
         * Out.
         *
         * @param ).print(c ).print(c
         * @return out result
         */
        return out().print(c);
    }

    /**
     * Print.
     *
     * @param i i
     * @return print result
     */
    public static NPrintStream print(int i) {
        /**
         * Out.
         *
         * @param ).print(i ).print(i
         * @return out result
         */
        return out().print(i);
    }

    /**
     * Print.
     *
     * @param l l
     * @return print result
     */
    public static NPrintStream print(long l) {
        /**
         * Out.
         *
         * @param ).print(l ).print(l
         * @return out result
         */
        return out().print(l);
    }

    /**
     * Print.
     *
     * @param f f
     * @return print result
     */
    public static NPrintStream print(float f) {
        /**
         * Out.
         *
         * @param ).print(f ).print(f
         * @return out result
         */
        return out().print(f);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(double d) {
        /**
         * Out.
         *
         * @param ).print(d ).print(d
         * @return out result
         */
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(char[] s) {
        /**
         * Out.
         *
         * @param ).print(s ).print(s
         * @return out result
         */
        return out().print(s);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Number d) {
        /**
         * Out.
         *
         * @param ).print(d ).print(d
         * @return out result
         */
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Temporal d) {
        /**
         * Out.
         *
         * @param ).print(d ).print(d
         * @return out result
         */
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Date d) {
        /**
         * Out.
         *
         * @param ).print(d ).print(d
         * @return out result
         */
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(String s) {
        /**
         * Out.
         *
         * @param ).print(s ).print(s
         * @return out result
         */
        return out().print(s);
    }

    /**
     * Print.
     *
     * @param obj obj
     * @return print result
     */
    public static NPrintStream print(Object obj) {
        /**
         * Out.
         *
         * @param ).print(obj ).print(obj
         * @return out result
         */
        return out().print(obj);
    }

    /**
     * Println.
     *
     * @return println result
     */
    public static NPrintStream println() {
        /**
         * Out.
         *
         * @param ).println( ).println(
         * @return out result
         */
        return out().println();
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Number d) {
        /**
         * Out.
         *
         * @param ).println(d ).println(d
         * @return out result
         */
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Temporal d) {
        /**
         * Out.
         *
         * @param ).println(d ).println(d
         * @return out result
         */
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Date d) {
        /**
         * Out.
         *
         * @param ).println(d ).println(d
         * @return out result
         */
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(boolean x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NMsg b) {
        /**
         * Out.
         *
         * @param ).println(b ).println(b
         * @return out result
         */
        return out().println(b);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NText b) {
        /**
         * Out.
         *
         * @param ).println(b ).println(b
         * @return out result
         */
        return out().println(b);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(int x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(long x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(float x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(double x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char[] x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(String x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(Object x) {
        /**
         * Out.
         *
         * @param ).println(x ).println(x
         * @return out result
         */
        return out().println(x);
    }

    /**
     * Trace.
     *
     * @param x x
     * @return trace result
     */
    public static NPrintStream trace(Supplier<NMsg> x) {
        NSession s = NSession.of();
        if (s.isTrace()) {
            if (x != null) {
                return s.out().println(x.get());
            }
        }
        /**
         * Out.
         *
         * @return out result
         */
        return out();
    }

    /**
     * Do with.
     *
     * @param x x
     */
    public static void doWith(Consumer<NPrintStream> x) {
        NSession s = NSession.of();
        if (s.isTrace()) {
            if (x != null) {
                x.accept(out());
            }
        }
    }

    /**
     * Trace.
     *
     * @param x x
     * @return trace result
     */
    public static NPrintStream trace(NMsg x) {
        NSession s = NSession.of();
        if (s.isTrace()) {
            if (x != null) {
                return s.out().println(x);
            }
        }
        /**
         * Out.
         *
         * @return out result
         */
        return out();
    }

    /**
     * Print.
     *
     * @param text text
     * @param style style
     * @return print result
     */
    public static NPrintStream print(Object text, NTextStyle style) {
        /**
         * Out.
         *
         * @param ).print(text ).print(text
         * @param style style
         * @return out result
         */
        return out().print(text, style);
    }

    /**
     * Print.
     *
     * @param text text
     * @param styles styles
     * @return print result
     */
    public static NPrintStream print(Object text, NTextStyles styles) {
        /**
         * Out.
         *
         * @param ).print(text ).print(text
         * @param styles styles
         * @return out result
         */
        return out().print(text, styles);
    }

    /**
     * Reset line.
     *
     * @return reset line result
     */
    public static NPrintStream resetLine() {
        /**
         * Out.
         *
         * @param ).resetLine( ).reset line(
         * @return out result
         */
        return out().resetLine();
    }

    /**
     * Print.
     *
     * @param csq csq
     * @return print result
     */
    public static NPrintStream print(CharSequence csq) {
        /**
         * Out.
         *
         * @param ).print(csq ).print(csq
         * @return out result
         */
        return out().print(csq);
    }

    /**
     * Print.
     *
     * @param csq csq
     * @param start start
     * @param end end
     * @return print result
     */
    public static NPrintStream print(CharSequence csq, int start, int end) {
        /**
         * Out.
         *
         * @param ).print(csq ).print(csq
         * @param start start
         * @param end end
         * @return out result
         */
        return out().print(csq, start, end);
    }

    /**
     * Terminal mode.
     *
     * @return terminal mode result
     */
    public static NTerminalMode terminalMode() {
        /**
         * Out.
         *
         * @param ).terminalMode( ).terminal mode(
         * @return out result
         */
        return out().terminalMode();
    }

    /**
     * Checks if is auto flash.
     *
     * @return is auto flash result
     */
    public static boolean isAutoFlash() {
        /**
         * Out.
         *
         * @param ).isAutoFlash( ).is auto flash(
         * @return out result
         */
        return out().isAutoFlash();
    }

    /**
     * Terminal mode.
     *
     * @param other other
     * @return terminal mode result
     */
    public static NPrintStream terminalMode(NTerminalMode other) {
        /**
         * Out.
         *
         * @param ).terminalMode(other ).terminal mode(other
         * @return out result
         */
        return out().terminalMode(other);
    }

    /**
     * Run.
     *
     * @param command command
     * @return run result
     */
    public static NPrintStream run(NTerminalCmd command) {
        /**
         * Out.
         *
         * @param ).run(command ).run(command
         * @return out result
         */
        return out().run(command);
    }

    /**
     * As output stream.
     *
     * @return as output stream result
     */
    public static OutputStream asOutputStream() {
        /**
         * Out.
         *
         * @param ).asOutputStream( ).as output stream(
         * @return out result
         */
        return out().asOutputStream();
    }

    /**
     * As print stream.
     *
     * @return as print stream result
     */
    public static PrintStream asPrintStream() {
        /**
         * Out.
         *
         * @param ).asPrintStream( ).as print stream(
         * @return out result
         */
        return out().asPrintStream();
    }

    /**
     * As writer.
     *
     * @return as writer result
     */
    public static Writer asWriter() {
        /**
         * Out.
         *
         * @param ).asWriter( ).as writer(
         * @return out result
         */
        return out().asWriter();
    }

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    public static boolean isNtf() {
        /**
         * Out.
         *
         * @param ).isNtf( ).is ntf(
         * @return out result
         */
        return out().isNtf();
    }

    /**
     * Terminal.
     *
     * @return terminal result
     */
    public static NSystemTerminalBase terminal() {
        /**
         * Out.
         *
         * @param ).terminal( ).terminal(
         * @return out result
         */
        return out().terminal();
    }

    /**
     * Out.
     *
     * @return out result
     */
    public static NPrintStream out() {
        NSession s = NSession.of();
        if (s.isTrace()) {
            return s.out();
        } else {
            return NPrintStream.NULL;
        }
    }
}
