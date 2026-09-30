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

/**
 * NOut class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NOut {
    /**
     * Flush.
     *
     * @return flush result
     */
    public static NPrintStream flush() {
        return out().flush();
    }


    /**
     * Close.
     *
     * @return close result
     */
    public static NPrintStream close() {
        out().close();
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
        return out().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(byte[] b) {
        return out().print(b);
    }

    /**
     * Write.
     *
     * @param b b
     * @return write result
     */
    public static NPrintStream write(int b) {
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NMsg b) {
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NText b) {
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(Boolean b) {
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(boolean b) {
        return out().print(b);
    }

    /**
     * Print.
     *
     * @param c c
     * @return print result
     */
    public static NPrintStream print(char c) {
        return out().print(c);
    }

    /**
     * Print.
     *
     * @param i i
     * @return print result
     */
    public static NPrintStream print(int i) {
        return out().print(i);
    }

    /**
     * Print.
     *
     * @param l l
     * @return print result
     */
    public static NPrintStream print(long l) {
        return out().print(l);
    }

    /**
     * Print.
     *
     * @param f f
     * @return print result
     */
    public static NPrintStream print(float f) {
        return out().print(f);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(double d) {
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(char[] s) {
        return out().print(s);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Number d) {
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Temporal d) {
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Date d) {
        return out().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(String s) {
        return out().print(s);
    }

    /**
     * Print.
     *
     * @param obj obj
     * @return print result
     */
    public static NPrintStream print(Object obj) {
        return out().print(obj);
    }

    /**
     * Println.
     *
     * @return println result
     */
    public static NPrintStream println() {
        return out().println();
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Number d) {
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Temporal d) {
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Date d) {
        return out().println(d);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(boolean x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NMsg b) {
        NPrintStream out = out();
        synchronized (out) {
            return out.println(b);
        }
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NText b) {
        NPrintStream out = out();
        synchronized (out) {
            return out.println(b);
        }
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(int x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(long x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(float x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(double x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char[] x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(String x) {
        return out().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(Object x) {
        return out().println(x);
    }

    /**
     * Print.
     *
     * @param text text
     * @param style style
     * @return print result
     */
    public static NPrintStream print(Object text, NTextStyle style) {
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
        return out().print(text, styles);
    }

    /**
     * Reset line.
     *
     * @return reset line result
     */
    public static NPrintStream resetLine() {
        return out().resetLine();
    }

    /**
     * Clear screen.
     *
     * @return clear screen result
     */
    public static NPrintStream clearScreen() {
        return out().run(NTerminalCmd.CLEAR_SCREEN);
    }

    /**
     * Clear line.
     *
     * @return clear line result
     */
    public static NPrintStream clearLine() {
        return out().run(NTerminalCmd.CLEAR_LINE);
    }

    /**
     * Clear line to cursor.
     *
     * @return clear line to cursor result
     */
    public static NPrintStream clearLineToCursor() {
        return out().run(NTerminalCmd.CLEAR_LINE_TO_CURSOR);
    }

    /**
     * Clear line from cursor.
     *
     * @return clear line from cursor result
     */
    public static NPrintStream clearLineFromCursor() {
        return out().run(NTerminalCmd.CLEAR_LINE_FROM_CURSOR);
    }

    /**
     * Move cursor up.
     *
     * @param count count
     * @return move cursor up result
     */
    public static NPrintStream moveCursorUp(int count) {
        return out().run(NTerminalCmd.MOVE_UP(count));
    }

    /**
     * Move cursor down.
     *
     * @param count count
     * @return move cursor down result
     */
    public static NPrintStream moveCursorDown(int count) {
        return out().run(NTerminalCmd.MOVE_DOWN(count));
    }

    /**
     * Move cursor left.
     *
     * @param count count
     * @return move cursor left result
     */
    public static NPrintStream moveCursorLeft(int count) {
        return out().run(NTerminalCmd.MOVE_LEFT(count));
    }

    /**
     * Move cursor right.
     *
     * @param count count
     * @return move cursor right result
     */
    public static NPrintStream moveCursorRight(int count) {
        return out().run(NTerminalCmd.MOVE_RIGHT(count));
    }

    /**
     * Move cursor up.
     *
     * @return move cursor up result
     */
    public static NPrintStream moveCursorUp() {
        return out().run(NTerminalCmd.MOVE_UP);
    }

    /**
     * Move cursor down.
     *
     * @return move cursor down result
     */
    public static NPrintStream moveCursorDown() {
        return out().run(NTerminalCmd.MOVE_DOWN);
    }

    /**
     * Move cursor left.
     *
     * @return move cursor left result
     */
    public static NPrintStream moveCursorLeft() {
        return out().run(NTerminalCmd.MOVE_LEFT);
    }

    /**
     * Move cursor right.
     *
     * @return move cursor right result
     */
    public static NPrintStream moveCursorRight() {
        return out().run(NTerminalCmd.MOVE_RIGHT);
    }

    /**
     * Move cursor to.
     *
     * @param column column
     * @param row row
     * @return move cursor to result
     */
    public static NPrintStream moveCursorTo(int column,int row) {
        return out().run(NTerminalCmd.MOVE_TO(column, row));
    }

    /**
     * Print.
     *
     * @param csq csq
     * @return print result
     */
    public static NPrintStream print(CharSequence csq) {
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
        return out().print(csq, start, end);
    }

    /**
     * Terminal mode.
     *
     * @return terminal mode result
     */
    public static NTerminalMode terminalMode() {
        return out().terminalMode();
    }

    /**
     * Checks if is auto flash.
     *
     * @return is auto flash result
     */
    public static boolean isAutoFlash() {
        return out().isAutoFlash();
    }

    /**
     * Terminal mode.
     *
     * @param other other
     * @return terminal mode result
     */
    public static NPrintStream terminalMode(NTerminalMode other) {
        return out().terminalMode(other);
    }

    /**
     * Run.
     *
     * @param command command
     * @return run result
     */
    public static NPrintStream run(NTerminalCmd command) {
        return out().run(command);
    }

    /**
     * As output stream.
     *
     * @return as output stream result
     */
    public static OutputStream asOutputStream() {
        return out().asOutputStream();
    }

    /**
     * As print stream.
     *
     * @return as print stream result
     */
    public static PrintStream asPrintStream() {
        return out().asPrintStream();
    }

    /**
     * As writer.
     *
     * @return as writer result
     */
    public static Writer asWriter() {
        return out().asWriter();
    }

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    public static boolean isNtf() {
        return out().isNtf();
    }

    /**
     * Terminal.
     *
     * @return terminal result
     */
    public static NSystemTerminalBase terminal() {
        return out().terminal();
    }

    /**
     * Out.
     *
     * @return out result
     */
    public static NPrintStream out() {
        return NSession.of().out();
    }

    /**
     * print progress with a message
     *
     * @param progress 0.0f-1.0f value
     * @param message  message
     * @since 0.8.6
     */
    public static void printProgress(float progress, NMsg message) {
        NSession.of().terminal().printProgress(progress, message);
    }

    /**
     * print indefinite progress with a message
     *
     * @param message message
     * @since 0.8.6
     */
    public static void printProgress(NMsg message) {
        NSession.of().terminal().printProgress(message);
    }

    /**
     * Checks if is plain.
     *
     * @return is plain result
     */
    public static boolean isPlain() {
        return NSession.of().isPlainOut();
    }
}
