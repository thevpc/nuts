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
 * NErr class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NErr {
    /**
     * Flush.
     *
     * @return flush result
     */
    public static NPrintStream flush(){
        /**
         * Err.
         *
         * @param ).flush( ).flush(
         * @return err result
         */
        return err().flush();
    }


    /**
     * Close.
     *
     * @return close result
     */
    public static NPrintStream close(){
      /**
       * Err.
       *
       * @param ).close( ).close(
       */
        err().close();
        /**
         * Err.
         *
         * @return err result
         */
        return err();
    }

    /**
     * Write raw.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write raw result
     */
    public static NPrintStream writeRaw(byte[] buf, int off, int len){
        /**
         * Err.
         *
         * @param ).writeRaw(buf ).write raw(buf
         * @param off off
         * @param len len
         * @return err result
         */
        return err().writeRaw(buf, off, len);
    }

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    public static NPrintStream write(byte[] buf, int off, int len){
        /**
         * Err.
         *
         * @param ).write(buf ).write(buf
         * @param off off
         * @param len len
         * @return err result
         */
        return err().write(buf, off, len);
    }

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    public static NPrintStream write(char[] buf, int off, int len){
        /**
         * Err.
         *
         * @param ).write(buf ).write(buf
         * @param off off
         * @param len len
         * @return err result
         */
        return err().write(buf, off, len);
    }

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    public static NPrintStream print(byte[] buf, int off, int len){
        /**
         * Err.
         *
         * @param ).print(buf ).print(buf
         * @param off off
         * @param len len
         * @return err result
         */
        return err().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    public static NPrintStream print(char[] buf, int off, int len){
        /**
         * Err.
         *
         * @param ).print(buf ).print(buf
         * @param off off
         * @param len len
         * @return err result
         */
        return err().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(byte[] b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Write.
     *
     * @param b b
     * @return write result
     */
    public static NPrintStream write(int b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NMsg b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NText b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(Boolean b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(boolean b){
        /**
         * Err.
         *
         * @param ).print(b ).print(b
         * @return err result
         */
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param c c
     * @return print result
     */
    public static NPrintStream print(char c){
        /**
         * Err.
         *
         * @param ).print(c ).print(c
         * @return err result
         */
        return err().print(c);
    }

    /**
     * Print.
     *
     * @param i i
     * @return print result
     */
    public static NPrintStream print(int i){
        /**
         * Err.
         *
         * @param ).print(i ).print(i
         * @return err result
         */
        return err().print(i);
    }

    /**
     * Print.
     *
     * @param l l
     * @return print result
     */
    public static NPrintStream print(long l){
        /**
         * Err.
         *
         * @param ).print(l ).print(l
         * @return err result
         */
        return err().print(l);
    }

    /**
     * Print.
     *
     * @param f f
     * @return print result
     */
    public static NPrintStream print(float f){
        /**
         * Err.
         *
         * @param ).print(f ).print(f
         * @return err result
         */
        return err().print(f);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(double d){
        /**
         * Err.
         *
         * @param ).print(d ).print(d
         * @return err result
         */
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(char[] s){
        /**
         * Err.
         *
         * @param ).print(s ).print(s
         * @return err result
         */
        return err().print(s);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Number d){
        /**
         * Err.
         *
         * @param ).print(d ).print(d
         * @return err result
         */
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Temporal d){
        /**
         * Err.
         *
         * @param ).print(d ).print(d
         * @return err result
         */
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Date d){
        /**
         * Err.
         *
         * @param ).print(d ).print(d
         * @return err result
         */
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(String s){
        /**
         * Err.
         *
         * @param ).print(s ).print(s
         * @return err result
         */
        return err().print(s);
    }

    /**
     * Print.
     *
     * @param obj obj
     * @return print result
     */
    public static NPrintStream print(Object obj){
        /**
         * Err.
         *
         * @param ).print(obj ).print(obj
         * @return err result
         */
        return err().print(obj);
    }

    /**
     * Println.
     *
     * @return println result
     */
    public static NPrintStream println(){
        /**
         * Err.
         *
         * @param ).println( ).println(
         * @return err result
         */
        return err().println();
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Number d){
        /**
         * Err.
         *
         * @param ).println(d ).println(d
         * @return err result
         */
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Temporal d){
        /**
         * Err.
         *
         * @param ).println(d ).println(d
         * @return err result
         */
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Date d){
        /**
         * Err.
         *
         * @param ).println(d ).println(d
         * @return err result
         */
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(boolean x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NMsg b){
        /**
         * Err.
         *
         * @param ).println(b ).println(b
         * @return err result
         */
        return err().println(b);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NText b){
        /**
         * Err.
         *
         * @param ).println(b ).println(b
         * @return err result
         */
        return err().println(b);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(int x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(long x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(float x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(double x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char[] x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(String x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(Object x){
        /**
         * Err.
         *
         * @param ).println(x ).println(x
         * @return err result
         */
        return err().println(x);
    }

    /**
     * Print.
     *
     * @param text text
     * @param style style
     * @return print result
     */
    public static NPrintStream print(Object text, NTextStyle style){
        /**
         * Err.
         *
         * @param ).print(text ).print(text
         * @param style style
         * @return err result
         */
        return err().print(text, style);
    }

    /**
     * Print.
     *
     * @param text text
     * @param styles styles
     * @return print result
     */
    public static NPrintStream print(Object text, NTextStyles styles){
        /**
         * Err.
         *
         * @param ).print(text ).print(text
         * @param styles styles
         * @return err result
         */
        return err().print(text, styles);
    }

    /**
     * Reset line.
     *
     * @return reset line result
     */
    public static NPrintStream resetLine(){
        /**
         * Err.
         *
         * @param ).resetLine( ).reset line(
         * @return err result
         */
        return err().resetLine();
    }

    /**
     * Print.
     *
     * @param csq csq
     * @return print result
     */
    public static NPrintStream print(CharSequence csq){
        /**
         * Err.
         *
         * @param ).print(csq ).print(csq
         * @return err result
         */
        return err().print(csq);
    }

    /**
     * Print.
     *
     * @param csq csq
     * @param start start
     * @param end end
     * @return print result
     */
    public static NPrintStream print(CharSequence csq, int start, int end){
        /**
         * Err.
         *
         * @param ).print(csq ).print(csq
         * @param start start
         * @param end end
         * @return err result
         */
        return err().print(csq, start, end);
    }

    /**
     * Terminal mode.
     *
     * @return terminal mode result
     */
    public static NTerminalMode terminalMode(){
        /**
         * Err.
         *
         * @param ).terminalMode( ).terminal mode(
         * @return err result
         */
        return err().terminalMode();
    }

    /**
     * Checks if is auto flash.
     *
     * @return is auto flash result
     */
    public static boolean isAutoFlash(){
        /**
         * Err.
         *
         * @param ).isAutoFlash( ).is auto flash(
         * @return err result
         */
        return err().isAutoFlash();
    }

    /**
     * Terminal mode.
     *
     * @param other other
     * @return terminal mode result
     */
    public static NPrintStream terminalMode(NTerminalMode other){
        /**
         * Err.
         *
         * @param ).terminalMode(other ).terminal mode(other
         * @return err result
         */
        return err().terminalMode(other);
    }

    /**
     * Run.
     *
     * @param command command
     * @return run result
     */
    public static NPrintStream run(NTerminalCmd command){
        /**
         * Err.
         *
         * @param ).run(command ).run(command
         * @return err result
         */
        return err().run(command);
    }

    /**
     * As output stream.
     *
     * @return as output stream result
     */
    public static OutputStream asOutputStream(){
        /**
         * Err.
         *
         * @param ).asOutputStream( ).as output stream(
         * @return err result
         */
        return err().asOutputStream();
    }

    /**
     * As print stream.
     *
     * @return as print stream result
     */
    public static PrintStream asPrintStream(){
        /**
         * Err.
         *
         * @param ).asPrintStream( ).as print stream(
         * @return err result
         */
        return err().asPrintStream();
    }

    /**
     * As writer.
     *
     * @return as writer result
     */
    public static Writer asWriter(){
        /**
         * Err.
         *
         * @param ).asWriter( ).as writer(
         * @return err result
         */
        return err().asWriter();
    }

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    public static boolean isNtf(){
        /**
         * Err.
         *
         * @param ).isNtf( ).is ntf(
         * @return err result
         */
        return err().isNtf();
    }

    /**
     * Terminal.
     *
     * @return terminal result
     */
    public static NSystemTerminalBase terminal(){
        /**
         * Err.
         *
         * @param ).terminal( ).terminal(
         * @return err result
         */
        return err().terminal();
    }

    /**
     * Err.
     *
     * @return err result
     */
    private static NPrintStream err() {
        return NSession.of().err();
    }
}
