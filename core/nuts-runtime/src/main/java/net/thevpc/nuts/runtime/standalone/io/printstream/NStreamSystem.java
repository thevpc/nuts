package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class NStreamSystem extends NStreamBase {
    private final OutputStream out;
    private PrintStream base;

    public NStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NSession session, NSystemTerminalBase term) {
        this(out, autoFlush, encoding, ansi, session, new Bindings(), term);
    }

    protected NStreamSystem(OutputStream out, PrintStream base, Boolean autoFlush,
                            NTerminalMode mode, NSession session, Bindings bindings, NSystemTerminalBase term) {
        super(autoFlush == null || autoFlush.booleanValue(), mode/*resolveMode(out,ansi, session)*/, session, bindings, term);
        //Do not use NutsTexts, not yet initialized!
        getOutputMetaData().setMessage(NMsg.ofStyled("<system-stream>", NTextStyle.path()));
        this.out = out;
        this.base = base;
    }

    public PrintStream getBase() {
        return base;
    }

    public NStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NSession session, Bindings bindings, NSystemTerminalBase term) {
        super(true, resolveMode(out, ansi, session), session, bindings, term);
        //Do not use NutsTexts, not yet initialized!
        getOutputMetaData().setMessage(NMsg.ofStyled("<system-stream>", NTextStyle.path()));
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
        switch (getTerminalMode()) {
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

    private static NTerminalMode resolveMode(OutputStream out, Boolean ansi, NSession session) {
        if (ansi != null) {
            return ansi ? NTerminalMode.ANSI : NTerminalMode.INHERITED;
        }
        NWorkspaceTerminalOptions b = session.boot().getBootTerminal();
        if (b.getFlags().contains("ansi")) {
            return NTerminalMode.ANSI;
        } else {
            return NTerminalMode.INHERITED;
        }
    }


    @Override
    public NStream flush() {
        base.flush();
        return this;
    }


    @Override
    public NStream close() {
        if (getTerminalMode() == NTerminalMode.ANSI) {
            write("\033[0m".getBytes());
            flush();
        }
        base.close();
        return this;
    }

    @Override
    public NStream write(int b) {
        base.write(b);
        return this;
    }

    @Override
    public NStream write(byte[] buf, int off, int len) {
        base.write(buf, off, len);
        return this;
    }

    @Override
    public NStream write(char[] buf, int off, int len) {
        if (buf == null) {
            base.print("null");
        } else {
            base.print(new String(buf, off, len));
        }
        return this;
    }

    @Override
    public NStream print(String s) {
        base.print(s);
        return this;
    }

    @Override
    public NStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NStreamSystem(out, base, autoFlash, getTerminalMode(), session, new Bindings(), getTerminal());
    }

    @Override
    public NStream print(char[] s) {
        base.print(s);
        return this;
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NStreamFormatted(this, getSession(), bindings);
            }
            case FILTERED: {
                return new NStreamFiltered(this, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
        switch (command.getName()) {
            case NTerminalCommand.Ids.GET_SIZE: {
                break;
            }
        }
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }

}
