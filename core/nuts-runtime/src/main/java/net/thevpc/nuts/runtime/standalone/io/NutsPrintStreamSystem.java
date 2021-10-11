package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.format.text.DefaultAnsiEscapeCommand;
import net.thevpc.nuts.runtime.core.util.CachedValue;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class NutsPrintStreamSystem extends NutsPrintStreamBase {
    private OutputStream out;
    private PrintStream base;
    private CachedValue<Integer> tput_cols;

    public NutsPrintStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NutsSession session) {
        this(out, autoFlush, encoding, ansi, session, new Bindings());
    }

    protected NutsPrintStreamSystem(OutputStream out, PrintStream base, CachedValue<Integer> tput_cols, Boolean autoFlush,
                                    NutsTerminalMode mode, NutsSession session, Bindings bindings) {
        super(autoFlush == null ? true : autoFlush.booleanValue(), mode/*resolveMode(out,ansi, session)*/, session, bindings);
        this.out = out;
        this.base = base;
    }

    public NutsPrintStreamSystem(OutputStream out, Boolean autoFlush, String encoding, Boolean ansi, NutsSession session, Bindings bindings) {
        super(true, resolveMode(out, ansi, session), session, bindings);
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
        NutsSession ws = session;
        //if(out==System.out || out==System.err){
        NutsOsFamily os = ws.env().getOsFamily();
        if (((os == NutsOsFamily.WINDOWS) && (CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM)) || os == NutsOsFamily.LINUX || os == NutsOsFamily.UNIX || os == NutsOsFamily.MACOS) {
            return NutsTerminalMode.ANSI;
        } else {
            return NutsTerminalMode.INHERITED;
        }
        //}
//        return NutsTerminalMode.INHERITED;
    }


//    public PrintStreamExtRaw(OutputStream out, NutsTerminalMode mode, NutsSession session) {
//        this(out, true, null, mode, session);
//    }
//
//    public PrintStreamExtRaw(String fileName, NutsTerminalMode mode, NutsSession session) throws FileNotFoundException {
//        this(new FileOutputStream(fileName), false, null, mode, session);
//    }
//
//    public PrintStreamExtRaw(String fileName, String csn, NutsTerminalMode mode, NutsSession session) throws FileNotFoundException, UnsupportedEncodingException {
//        this(new FileOutputStream(fileName), false, csn, mode, session);
//    }
//
//    public PrintStreamExtRaw(File file, NutsTerminalMode mode, NutsSession session) throws FileNotFoundException {
//        this(new FileOutputStream(file), null, null, mode, session);
//    }
//
//    public PrintStreamExtRaw(File file, String csn, NutsTerminalMode mode, NutsSession session) throws FileNotFoundException, UnsupportedEncodingException {
//        this(new FileOutputStream(file), null, csn, mode, session);
//    }

    //    @Override
//    public void setSession(NutsSession session) {
//        this.session = session;
////        this.ws=session==null?null:session.getWorkspace();
//    }
//
    @Override
    public NutsPrintStream flush() {
        base.flush();
        return this;
    }


//    @Override
//    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
//        if (other == null || other == getModeOp()) {
//            return this;
//        }
//        if (out instanceof ExtendedFormatAware) {
//            return ((ExtendedFormatAware) out).convert(other);
//        }
//        return new RawOutputStream(out, session).convert(other);
//    }

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
    public NutsPrintStream convertSession(NutsSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NutsPrintStreamSystem(out, base, tput_cols, autoFlash, mode(), session, new Bindings());
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command) {
        if (mode() == NutsTerminalMode.ANSI) {
            // TODO!!
            //should re-implement this!!
            switch (command.getName()) {
                case NutsTerminalCommand.Ids
                        .CLEAR_LINE: {
                    //printf("%s", session.text().forCommand(command));
                    break;
                }
                case NutsTerminalCommand.Ids
                        .CLEAR_LINE_FROM_CURSOR: {
                    //printf("%s", session.text().forCommand(command));
                    break;
                }
            }
            flush();
        }
        return this;
    }

    @Override
    public int getColumns() {
        int tputCallTimeout = session.boot().getCustomBootOption("nuts.term.tput.call.timeout").getInt(60);
        Integer w = session.boot().getCustomBootOption("nuts.term.width").getInt(null);
        if (w == null) {
            if (tput_cols == null) {
                tput_cols = new CachedValue<>(new DefaultAnsiEscapeCommand.TputEvaluator(session), tputCallTimeout);
            }
            Integer v = tput_cols.getValue();
            return v == null ? -1 : v;
        }
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
                return new NutsPrintStreamFormatted(this, bindings);
            }
            case FILTERED: {
                return new NutsPrintStreamFiltered(this, bindings);
            }
        }
        throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

//
//    protected void baseWriteChars(char[] chars) {
//        switch (mode) {
//            case FILTERED: {
//                try {
//                    //ESCAPE
//                    base.write(
//                            DefaultNutsTextNodeParser.escapeText0(new String(chars)).getBytes()
//                    );
//                } catch (IOException e) {
//                    trouble = true;
//                }
//                break;
//            }
//        }
//    }
}
