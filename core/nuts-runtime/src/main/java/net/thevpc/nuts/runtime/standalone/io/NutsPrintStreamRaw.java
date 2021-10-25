package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Print stream from custom output streams like ByteArrayOutputStream
 */
public class NutsPrintStreamRaw extends NutsPrintStreamBase {
    protected OutputStream out;
    private PrintStream base;

    protected NutsPrintStreamRaw(OutputStream out, PrintStream base, Boolean autoFlush, NutsTerminalMode mode, NutsSession session, Bindings bindings) {
        super(autoFlush == null ? true : autoFlush, mode, session, bindings);
        this.out = out;
        this.base = base;
    }

    public NutsPrintStreamRaw(OutputStream out, Boolean autoFlush, String encoding, NutsSession session, Bindings bindings) {
        super(true, NutsTerminalMode.INHERITED, session, bindings);
        this.out = out;
        if (out instanceof PrintStream) {
            PrintStream ps = (PrintStream) out;
            if (autoFlush == null && encoding == null) {
                base = ps;
            }
        }
        if (base == null) {
            try {
                this.base =
                        encoding == null ?
                                new PrintStream(out, autoFlush != null && autoFlush)
                                : new PrintStream(out, autoFlush != null && autoFlush, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        switch (mode()) {
            case ANSI: {
                if (bindings.ansi != null) {
                    throw new IllegalArgumentException("already bound ansi");
                }
                bindings.ansi = this;
                if (bindings.inherited == null) {
                    bindings.inherited = this;
                }
                break;
            }
            case INHERITED: {
                if (bindings.inherited != null) {
                    throw new IllegalArgumentException("already bound ansi");
                }
                bindings.inherited = this;
                break;
            }
        }
    }

    public PrintStream getBase() {
        return base;
    }

    @Override
    public NutsPrintStream flush() {
        base.flush();
        return this;
    }

    @Override
    public NutsPrintStream close() {
        if (mode() == NutsTerminalMode.ANSI) {
            write("\033[0m".getBytes());
            flush();
        }
        base.close();
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        base.write(b);
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        base.write(buf, off, len);
        return this;
    }

    @Override
    public NutsPrintStream write(char[] s, int off, int len) {
        if (s == null) {
            base.print("null");
        } else {
            base.print(new String(s, off, len));
        }
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        base.print(s);
        return this;
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NutsPrintStreamRaw(out, base, autoFlash, mode(), session, new Bindings());
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command) {
        return this;
    }

    @Override
    public int getColumns() {
        return -1;
    }

    @Override
    public NutsPrintStream print(char[] s) {
        base.print(s);
        return this;
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NutsPrintStreamFormatted(this,getSession(), bindings);
            }
            case FILTERED: {
                return new NutsPrintStreamFiltered(this, getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }
}
