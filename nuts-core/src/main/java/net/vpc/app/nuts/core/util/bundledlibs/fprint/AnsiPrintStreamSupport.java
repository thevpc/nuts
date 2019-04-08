package net.vpc.app.nuts.core.util.bundledlibs.fprint;

import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiOutputStream;
import org.fusesource.jansi.internal.CLibrary;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class AnsiPrintStreamSupport extends PrintStream {

    static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    static final boolean IS_CYGWIN = IS_WINDOWS
            && System.getenv("PWD") != null
            && System.getenv("PWD").startsWith("/")
            && !"cygwin".equals(System.getenv("TERM"));

    static final boolean IS_MINGW_XTERM = IS_WINDOWS
            && System.getenv("MSYSTEM") != null
            && System.getenv("MSYSTEM").startsWith("MINGW")
            && "xterm".equals(System.getenv("TERM"));

    public enum Type {
        STRIP,
        INHERIT,
        ANSI,
    }

    public static void uninstall() {
        uninstallStdOut();
        uninstallStdErr();
    }

    public static void install(Type type) {
        installStdOut(type);
        installStdErr(type);
    }

    public static void installStdOut(Type type) {
        PrintStream out = System.out;
        if (out instanceof AnsiPrintStreamSupport) {
            AnsiPrintStreamSupport pout = (AnsiPrintStreamSupport) out;
            ((MyOutputStream) pout.out).setType(type);
        } else {
            System.setOut(new AnsiPrintStreamSupport(out, CLibrary.STDOUT_FILENO, type));
        }
    }

    public static void uninstallStdOut() {
        PrintStream out = System.out;
        if (out instanceof AnsiPrintStreamSupport) {
            AnsiPrintStreamSupport pout = (AnsiPrintStreamSupport) out;
            System.setOut((PrintStream) ((MyOutputStream) pout.out).base);
        } else {
            //
        }
    }

    public static void installStdErr(Type type) {
        PrintStream err = System.err;
        if (err instanceof AnsiPrintStreamSupport) {
            AnsiPrintStreamSupport pout = (AnsiPrintStreamSupport) err;
            ((MyOutputStream) pout.out).setType(type);
        } else {
            System.setErr(new AnsiPrintStreamSupport(System.err, CLibrary.STDERR_FILENO, type));
        }
    }

    public static void uninstallStdErr() {
        PrintStream out = System.err;
        if (out instanceof AnsiPrintStreamSupport) {
            AnsiPrintStreamSupport pout = (AnsiPrintStreamSupport) out;
            System.setErr((PrintStream) ((MyOutputStream) pout.out).base);
        } else {
            //
        }
    }


    public AnsiPrintStreamSupport(OutputStream out, int fileno, Type type) {
        super(new MyOutputStream(out, fileno, type),true);
    }

    private static class MyOutputStream extends FilterOutputStream {
        private int fileno;
        private Type type;
        private OutputStream base;
        private OutputStream baseStripped;
        private OutputStream ansi;

        public MyOutputStream(OutputStream base, int fileno, Type type) {
            super(base);
            this.fileno = fileno;
            this.type = type;
            this.base = base;
            this.baseStripped = new AnsiOutputStream(base);
            if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM) {
                // On windows we know the console does not interpret ANSI codes..
                try {
                    this.ansi = new WindowsAnsiOutputStream(base);
                } catch (Throwable ignore) {
                    this.ansi = new AnsiOutputStream(base);
                }
            } else {
                ansi = new FilterOutputStream(base) {
                    @Override
                    public void close() throws IOException {
                        write(AnsiOutputStream.RESET_CODE);
                        flush();
                        super.close();
                    }
                };
            }
            setType(type);
        }

        public void setType(Type type) {
            if (type == null) {
                type = Type.ANSI;
            }
            this.type = type;
            switch (type) {
                case INHERIT: {
                    super.out = base;
                    break;
                }
                case ANSI: {
                    super.out = ansi;
                    break;
                }
                case STRIP: {
                    super.out = baseStripped;
                    break;
                }
            }
        }

    }
}
