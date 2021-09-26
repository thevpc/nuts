package net.thevpc.nuts;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;

public interface NutsPrintStream {
    static NutsPrintStream ofNull(NutsSession session) {
        return session.getWorkspace().io().nullPrintStream();
    }

    static NutsPrintStream ofMemory(NutsSession session) {
        return session.getWorkspace().io().createMemoryPrintStream();
    }

    static NutsPrintStream of(OutputStream out, NutsSession session) {
        return session.getWorkspace().io().createPrintStream(out);
    }

    static NutsPrintStream of(OutputStream out, NutsTerminalMode mode, NutsSession session) {
        return session.getWorkspace().io().createPrintStream(out, mode);
    }

    static NutsPrintStream of(Writer out, NutsSession session) {
        return session.getWorkspace().io().createPrintStream(out);
    }

    NutsPrintStream flush();

    NutsPrintStream close();

    NutsPrintStream write(byte[] b);

    NutsPrintStream write(int b);

    NutsPrintStream write(byte[] buf, int off, int len);

    NutsPrintStream write(char[] buf);

    NutsPrintStream write(char[] buf, int off, int len);

    NutsPrintStream print(NutsString b);

    NutsPrintStream print(boolean b);

    NutsPrintStream print(char c);

    NutsPrintStream print(int i);

    NutsPrintStream print(long l);

    NutsPrintStream print(float f);

    NutsPrintStream print(double d);

    NutsPrintStream print(char[] s);

    NutsPrintStream print(String s);

    NutsPrintStream print(Object obj);

    NutsPrintStream println();

    NutsPrintStream println(boolean x);

    NutsPrintStream println(char x);

    NutsPrintStream println(NutsString b);

    NutsPrintStream println(int x);

    NutsPrintStream println(long x);

    NutsPrintStream println(float x);

    NutsPrintStream println(double x);

    NutsPrintStream println(char[] x);

    NutsPrintStream println(String x);

    NutsPrintStream println(Object x);

    NutsPrintStream resetLine();

    NutsPrintStream printf(String format, Object... args);

    /**
     * print java formatted string (with {})
     * {@code
     * printj("{1,choice,0#|1# 1 file|1< {1} files}",nbr);
     * }
     *
     * @param format java style format (with {})
     * @param args   format args
     * @return {@code this} instance
     */
    NutsPrintStream printj(String format, Object... args);

    NutsPrintStream printf(Locale l, String format, Object... args);

    NutsPrintStream format(String format, Object... args);

    NutsPrintStream format(Locale l, String format, Object... args);

    NutsPrintStream append(CharSequence csq);

    NutsPrintStream append(CharSequence csq, int start, int end);

    NutsPrintStream append(char c);

    NutsTerminalMode mode();

    boolean isAutoFlash();

    NutsPrintStream convertMode(NutsTerminalMode other);

    NutsPrintStream convertSession(NutsSession session);

    NutsPrintStream run(NutsTerminalCommand command);

    int getColumns();

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();
}
