package net.thevpc.nuts.io;

import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public class NullNPrintStream implements NPrintStream {

    public static NPrintStream INSTANCE =new NullNPrintStream();

    private NullNPrintStream() {
    }

    @Override
    public OutputStream getOutputStream() {
        return NullOutputStream.INSTANCE;
    }

    ////////////

    private DefaultNContentMetadata md = new DefaultNContentMetadata();


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
        return this;
    }

    @Override
    public NPrintStream write(int b) {
        return this;
    }

    @Override
    public NPrintStream print(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NPrintStream print(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NPrintStream print(NMsg b) {
        return this;
    }

    @Override
    public NPrintStream print(NText b) {
        return this;
    }


    @Override
    public NPrintStream print(boolean b) {
        return this;
    }

    @Override
    public NPrintStream print(char c) {
        return this;
    }

    @Override
    public NPrintStream print(int i) {
        return this;
    }

    @Override
    public NPrintStream print(long l) {
        return this;
    }

    @Override
    public NPrintStream print(float f) {
        return this;
    }

    @Override
    public NPrintStream print(double d) {
        return this;
    }

    @Override
    public NPrintStream print(char[] s) {
        return this;
    }

    @Override
    public NPrintStream print(String s) {
        return this;
    }

    @Override
    public NPrintStream print(Object obj) {
        return this;
    }

    @Override
    public NPrintStream println() {
        return this;
    }

    @Override
    public NPrintStream println(boolean x) {
        return this;
    }

    @Override
    public NPrintStream println(char x) {
        return this;
    }

    @Override
    public NPrintStream println(NMsg b) {
        return this;
    }

    @Override
    public NPrintStream println(NText b) {
        return this;
    }

    @Override
    public NPrintStream println(int x) {
        return this;
    }

    @Override
    public NPrintStream println(long x) {
        return this;
    }

    @Override
    public NPrintStream println(float x) {
        return this;
    }

    @Override
    public NPrintStream println(double x) {
        return this;
    }

    @Override
    public NPrintStream println(char[] x) {
        return this;
    }

    @Override
    public NPrintStream println(String x) {
        return this;
    }

    @Override
    public NPrintStream println(Object x) {
        return this;
    }

    @Override
    public NPrintStream print(Object text, NTextStyle style) {
        return this;
    }

    @Override
    public NPrintStream print(Object text, NTextStyles styles) {
        return this;
    }

    @Override
    public NPrintStream resetLine() {
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq) {
        return this;
    }

    @Override
    public NPrintStream print(CharSequence csq, int start, int end) {
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
        return NullOutputStream.INSTANCE;
    }

    @Override
    public PrintStream asPrintStream() {
        return new PrintStream(asOutputStream());
    }

    @Override
    public Writer asWriter() {
        return NullWriter.INSTANCE;
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
        return "";
    }

    @Override
    public NPrintStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NPrintStream write(char[] buf, int off, int len) {
        return this;
    }

    public NPrintStream printNull() {
        return this;
    }

    @Override
    public NPrintStream print(Boolean b) {
        return this;
    }

    @Override
    public NPrintStream print(Number b) {
        return this;
    }

    @Override
    public NPrintStream print(Temporal b) {
        return this;
    }

    @Override
    public NPrintStream print(Date b) {
        return this;
    }

    @Override
    public NPrintStream println(Number b) {
        return this;
    }

    @Override
    public NPrintStream println(Temporal b) {
        return this;
    }

    @Override
    public NPrintStream println(Date b) {
        return this;
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        return this;
    }
}
