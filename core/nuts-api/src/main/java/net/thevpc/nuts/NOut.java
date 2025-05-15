package net.thevpc.nuts;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public class NOut {
    public static NPrintStream flush(){
        return out().flush();
    }


    public static NPrintStream close(){
        out().close();
        return out();
    }

    public static NPrintStream writeRaw(byte[] buf, int off, int len){
        return out().writeRaw(buf, off, len);
    }

    public static NPrintStream write(byte[] buf, int off, int len){
        return out().write(buf, off, len);
    }

    public static NPrintStream write(char[] buf, int off, int len){
        return out().write(buf, off, len);
    }

    public static NPrintStream print(byte[] buf, int off, int len){
        return out().print(buf, off, len);
    }

    public static NPrintStream print(char[] buf, int off, int len){
        return out().print(buf, off, len);
    }

    public static NPrintStream print(byte[] b){
        return out().print(b);
    }

    public static NPrintStream write(int b){
        return out().print(b);
    }

    public static NPrintStream print(NMsg b){
        return out().print(b);
    }

    public static NPrintStream print(NText b){
        return out().print(b);
    }

    public static NPrintStream print(Boolean b){
        return out().print(b);
    }

    public static NPrintStream print(boolean b){
        return out().print(b);
    }

    public static NPrintStream print(char c){
        return out().print(c);
    }

    public static NPrintStream print(int i){
        return out().print(i);
    }

    public static NPrintStream print(long l){
        return out().print(l);
    }

    public static NPrintStream print(float f){
        return out().print(f);
    }

    public static NPrintStream print(double d){
        return out().print(d);
    }

    public static NPrintStream print(char[] s){
        return out().print(s);
    }

    public static NPrintStream print(Number d){
        return out().print(d);
    }

    public static NPrintStream print(Temporal d){
        return out().print(d);
    }

    public static NPrintStream print(Date d){
        return out().print(d);
    }

    public static NPrintStream print(String s){
        return out().print(s);
    }

    public static NPrintStream print(Object obj){
        return out().print(obj);
    }

    public static NPrintStream println(){
        return out().println();
    }

    public static NPrintStream println(Number d){
        return out().println(d);
    }

    public static NPrintStream println(Temporal d){
        return out().println(d);
    }

    public static NPrintStream println(Date d){
        return out().println(d);
    }

    public static NPrintStream println(boolean x){
        return out().println(x);
    }

    public static NPrintStream println(char x){
        return out().println(x);
    }

    public static NPrintStream println(NMsg b){
        return out().println(b);
    }

    public static NPrintStream println(NText b){
        return out().println(b);
    }

    public static NPrintStream println(int x){
        return out().println(x);
    }

    public static NPrintStream println(long x){
        return out().println(x);
    }

    public static NPrintStream println(float x){
        return out().println(x);
    }

    public static NPrintStream println(double x){
        return out().println(x);
    }

    public static NPrintStream println(char[] x){
        return out().println(x);
    }

    public static NPrintStream println(String x){
        return out().println(x);
    }

    public static NPrintStream println(Object x){
        return out().println(x);
    }

    public static NPrintStream print(Object text, NTextStyle style){
        return out().print(text, style);
    }

    public static NPrintStream print(Object text, NTextStyles styles){
        return out().print(text, styles);
    }

    public static NPrintStream resetLine(){
        return out().resetLine();
    }

    public static NPrintStream print(CharSequence csq){
        return out().print(csq);
    }

    public static NPrintStream print(CharSequence csq, int start, int end){
        return out().print(csq, start, end);
    }

    public static NTerminalMode getTerminalMode(){
        return out().getTerminalMode();
    }

    public static boolean isAutoFlash(){
        return out().isAutoFlash();
    }

    public static NPrintStream setTerminalMode(NTerminalMode other){
        return out().setTerminalMode(other);
    }

    public static NPrintStream run(NTerminalCmd command){
        return out().run(command);
    }

    public static OutputStream asOutputStream(){
        return out().asOutputStream();
    }

    public static PrintStream asPrintStream(){
        return out().asPrintStream();
    }

    public static Writer asWriter(){
        return out().asWriter();
    }

    public static boolean isNtf(){
        return out().isNtf();
    }

    public static NSystemTerminalBase terminal(){
        return out().getTerminal();
    }

    public static NPrintStream out() {
        return NSession.of().out();
    }
}
