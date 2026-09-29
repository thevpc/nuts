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
        return err().flush();
    }


    /**
     * Close.
     *
     * @return close result
     */
    public static NPrintStream close(){
        err().close();
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
        return err().print(buf, off, len);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(byte[] b){
        return err().print(b);
    }

    /**
     * Write.
     *
     * @param b b
     * @return write result
     */
    public static NPrintStream write(int b){
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NMsg b){
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(NText b){
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(Boolean b){
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    public static NPrintStream print(boolean b){
        return err().print(b);
    }

    /**
     * Print.
     *
     * @param c c
     * @return print result
     */
    public static NPrintStream print(char c){
        return err().print(c);
    }

    /**
     * Print.
     *
     * @param i i
     * @return print result
     */
    public static NPrintStream print(int i){
        return err().print(i);
    }

    /**
     * Print.
     *
     * @param l l
     * @return print result
     */
    public static NPrintStream print(long l){
        return err().print(l);
    }

    /**
     * Print.
     *
     * @param f f
     * @return print result
     */
    public static NPrintStream print(float f){
        return err().print(f);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(double d){
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(char[] s){
        return err().print(s);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Number d){
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Temporal d){
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    public static NPrintStream print(Date d){
        return err().print(d);
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public static NPrintStream print(String s){
        return err().print(s);
    }

    /**
     * Print.
     *
     * @param obj obj
     * @return print result
     */
    public static NPrintStream print(Object obj){
        return err().print(obj);
    }

    /**
     * Println.
     *
     * @return println result
     */
    public static NPrintStream println(){
        return err().println();
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Number d){
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Temporal d){
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    public static NPrintStream println(Date d){
        return err().println(d);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(boolean x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NMsg b){
        return err().println(b);
    }

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    public static NPrintStream println(NText b){
        return err().println(b);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(int x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(long x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(float x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(double x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(char[] x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(String x){
        return err().println(x);
    }

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    public static NPrintStream println(Object x){
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
        return err().print(text, styles);
    }

    /**
     * Reset line.
     *
     * @return reset line result
     */
    public static NPrintStream resetLine(){
        return err().resetLine();
    }

    /**
     * Print.
     *
     * @param csq csq
     * @return print result
     */
    public static NPrintStream print(CharSequence csq){
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
        return err().print(csq, start, end);
    }

    /**
     * Terminal mode.
     *
     * @return terminal mode result
     */
    public static NTerminalMode terminalMode(){
        return err().terminalMode();
    }

    /**
     * Checks if is auto flash.
     *
     * @return is auto flash result
     */
    public static boolean isAutoFlash(){
        return err().isAutoFlash();
    }

    /**
     * Terminal mode.
     *
     * @param other other
     * @return terminal mode result
     */
    public static NPrintStream terminalMode(NTerminalMode other){
        return err().terminalMode(other);
    }

    /**
     * Run.
     *
     * @param command command
     * @return run result
     */
    public static NPrintStream run(NTerminalCmd command){
        return err().run(command);
    }

    /**
     * As output stream.
     *
     * @return as output stream result
     */
    public static OutputStream asOutputStream(){
        return err().asOutputStream();
    }

    /**
     * As print stream.
     *
     * @return as print stream result
     */
    public static PrintStream asPrintStream(){
        return err().asPrintStream();
    }

    /**
     * As writer.
     *
     * @return as writer result
     */
    public static Writer asWriter(){
        return err().asWriter();
    }

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    public static boolean isNtf(){
        return err().isNtf();
    }

    /**
     * Terminal.
     *
     * @return terminal result
     */
    public static NSystemTerminalBase terminal(){
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
