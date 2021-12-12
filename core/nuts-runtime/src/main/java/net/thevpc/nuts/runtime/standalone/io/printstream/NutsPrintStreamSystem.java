package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextStyled;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class NutsPrintStreamSystem extends NutsPrintStreamBase{
    private final OutputStream out;
    private PrintStream base;

    public NutsPrintStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NutsSession session, NutsSystemTerminalBase term) {
        this(out, autoFlush, encoding, ansi, session, new Bindings(),term);
    }

    protected NutsPrintStreamSystem(OutputStream out, PrintStream base, Boolean autoFlush,
                                    NutsTerminalMode mode, NutsSession session, Bindings bindings, NutsSystemTerminalBase term) {
        super(autoFlush == null || autoFlush.booleanValue(), mode/*resolveMode(out,ansi, session)*/, session, bindings,term);
        //Do not use NutsTexts, not yet initialized!
        setFormattedName(new DefaultNutsTextStyled(session,new DefaultNutsTextPlain(session,"<system-stream>" ),NutsTextStyles.of(NutsTextStyle.path())));
        this.out = out;
        this.base = base;
    }

    public PrintStream getBase() {
        return base;
    }

    public NutsPrintStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NutsSession session, Bindings bindings,NutsSystemTerminalBase term) {
        super(true, resolveMode(out, ansi, session), session, bindings,term);
        //Do not use NutsTexts, not yet initialized!
        setFormattedName(new DefaultNutsTextStyled(session,new DefaultNutsTextPlain(session,"<system-stream>" ),NutsTextStyles.of(NutsTextStyle.path())));
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

    private static NutsTerminalMode resolveMode(OutputStream out, Boolean ansi, NutsSession session) {
        if (ansi != null) {
            return ansi ? NutsTerminalMode.ANSI : NutsTerminalMode.INHERITED;
        }
        NutsBootTerminal b = session.boot().getBootTerminal();
        if (b.getFlags().contains("ansi")) {
            return NutsTerminalMode.ANSI;
        } else {
            return NutsTerminalMode.INHERITED;
        }
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
    public NutsPrintStream write(char[] buf, int off, int len) {
        if (buf == null) {
            base.print("null");
        } else {
            base.print(new String(buf, off, len));
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
        return new NutsPrintStreamSystem(out, base, autoFlash, mode(), session, new Bindings(),getTerminal());
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
                return new NutsPrintStreamFormatted(this, getSession(), bindings);
            }
            case FILTERED: {
                return new NutsPrintStreamFiltered(this, getSession(), bindings);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported %s -> %s", mode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        switch (command.getName()){
            case NutsTerminalCommand.Ids.GET_SIZE:{
                break;
            }
        }
        return null;
    }
}
