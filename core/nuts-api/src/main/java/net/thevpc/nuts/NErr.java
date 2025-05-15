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

public class NErr {
    public static NPrintStream flush(){
        return err().flush();
    }


    public static NPrintStream close(){
        err().close();
        return err();
    }

    public static NPrintStream writeRaw(byte[] buf, int off, int len){
        return err().writeRaw(buf, off, len);
    }

    public static NPrintStream write(byte[] buf, int off, int len){
        return err().write(buf, off, len);
    }

    public static NPrintStream write(char[] buf, int off, int len){
        return err().write(buf, off, len);
    }

    public static NPrintStream print(byte[] buf, int off, int len){
        return err().print(buf, off, len);
    }

    public static NPrintStream print(char[] buf, int off, int len){
        return err().print(buf, off, len);
    }

    public static NPrintStream print(byte[] b){
        return err().print(b);
    }

    public static NPrintStream write(int b){
        return err().print(b);
    }

    public static NPrintStream print(NMsg b){
        return err().print(b);
    }

    public static NPrintStream print(NText b){
        return err().print(b);
    }

    public static NPrintStream print(Boolean b){
        return err().print(b);
    }

    public static NPrintStream print(boolean b){
        return err().print(b);
    }

    public static NPrintStream print(char c){
        return err().print(c);
    }

    public static NPrintStream print(int i){
        return err().print(i);
    }

    public static NPrintStream print(long l){
        return err().print(l);
    }

    public static NPrintStream print(float f){
        return err().print(f);
    }

    public static NPrintStream print(double d){
        return err().print(d);
    }

    public static NPrintStream print(char[] s){
        return err().print(s);
    }

    public static NPrintStream print(Number d){
        return err().print(d);
    }

    public static NPrintStream print(Temporal d){
        return err().print(d);
    }

    public static NPrintStream print(Date d){
        return err().print(d);
    }

    public static NPrintStream print(String s){
        return err().print(s);
    }

    public static NPrintStream print(Object obj){
        return err().print(obj);
    }

    public static NPrintStream println(){
        return err().println();
    }

    public static NPrintStream println(Number d){
        return err().println(d);
    }

    public static NPrintStream println(Temporal d){
        return err().println(d);
    }

    public static NPrintStream println(Date d){
        return err().println(d);
    }

    public static NPrintStream println(boolean x){
        return err().println(x);
    }

    public static NPrintStream println(char x){
        return err().println(x);
    }

    public static NPrintStream println(NMsg b){
        return err().println(b);
    }

    public static NPrintStream println(NText b){
        return err().println(b);
    }

    public static NPrintStream println(int x){
        return err().println(x);
    }

    public static NPrintStream println(long x){
        return err().println(x);
    }

    public static NPrintStream println(float x){
        return err().println(x);
    }

    public static NPrintStream println(double x){
        return err().println(x);
    }

    public static NPrintStream println(char[] x){
        return err().println(x);
    }

    public static NPrintStream println(String x){
        return err().println(x);
    }

    public static NPrintStream println(Object x){
        return err().println(x);
    }

    public static NPrintStream print(Object text, NTextStyle style){
        return err().print(text, style);
    }

    public static NPrintStream print(Object text, NTextStyles styles){
        return err().print(text, styles);
    }

    public static NPrintStream resetLine(){
        return err().resetLine();
    }

    public static NPrintStream print(CharSequence csq){
        return err().print(csq);
    }

    public static NPrintStream print(CharSequence csq, int start, int end){
        return err().print(csq, start, end);
    }

    public static NTerminalMode getTerminalMode(){
        return err().getTerminalMode();
    }

    public static boolean isAutoFlash(){
        return err().isAutoFlash();
    }

    public static NPrintStream setTerminalMode(NTerminalMode other){
        return err().setTerminalMode(other);
    }

    public static NPrintStream run(NTerminalCmd command){
        return err().run(command);
    }

    public static OutputStream asOutputStream(){
        return err().asOutputStream();
    }

    public static PrintStream asPrintStream(){
        return err().asPrintStream();
    }

    public static Writer asWriter(){
        return err().asWriter();
    }

    public static boolean isNtf(){
        return err().isNtf();
    }

    public static NSystemTerminalBase terminal(){
        return err().getTerminal();
    }

    private static NPrintStream err() {
        return NSession.of().err();
    }
}
