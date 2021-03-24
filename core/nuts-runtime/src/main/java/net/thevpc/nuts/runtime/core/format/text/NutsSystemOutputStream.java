package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.io.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;

public class NutsSystemOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    private NutsTerminalMode type;
    private final OutputStream base;
    private final OutputStream baseStripped;
    private final OutputStream formatted;
    private final NutsWorkspace ws;
    private final NutsSession session;

    public NutsSystemOutputStream(OutputStream base, NutsTerminalMode type, NutsSession session) {
        super(base);
        this.session = session;
        this.ws = session.getWorkspace();
        this.type = type;
        this.base = base;
        this.baseStripped = CoreIOUtils.convertOutputStream(base, NutsTerminalMode.FILTERED, session);
        if (ws.env().getOptionAsBoolean("enableJansi",false) && OptionalJansi.isAvailable()) {
            OutputStream f = OptionalJansi.preparestream(base);
            if(f!=null){
                this.formatted = CoreIOUtils.convertOutputStream(base, NutsTerminalMode.FORMATTED, session);
                setType(type);
            }else{
                this.formatted = baseStripped;
                setType(NutsTerminalMode.FILTERED);
            }
        }else{
            NutsOsFamily os = ws.env().getOsFamily();
            boolean IS_WINDOWS = os == NutsOsFamily.WINDOWS;
            boolean IS_CYGWIN = IS_WINDOWS
                    && System.getenv("PWD") != null
                    && System.getenv("PWD").startsWith("/")
                    && !"cygwin".equals(System.getenv("TERM"));

            boolean IS_MINGW_XTERM = IS_WINDOWS
                    && System.getenv("MSYSTEM") != null
                    && System.getenv("MSYSTEM").startsWith("MINGW")
                    && "xterm".equals(System.getenv("TERM"));
            if ((IS_WINDOWS && (IS_CYGWIN || IS_MINGW_XTERM)) || os == NutsOsFamily.LINUX || os == NutsOsFamily.UNIX || os == NutsOsFamily.MACOS) {
                FilterOutputStream filterOutputStream = new AnsiResetOnCloseOutputStream(base);
                this.formatted = CoreIOUtils.convertOutputStream(filterOutputStream, NutsTerminalMode.FORMATTED, session);
                setType(type);
            } else {
                this.formatted = baseStripped;
                setType(NutsTerminalMode.FILTERED);
            }
        }
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        synchronized (this) {
            out.write(bytes, off, len);
        }
    }

    public void setType(NutsTerminalMode type) {
        if (type == null) {
            type = NutsTerminalMode.FORMATTED;
        }
        this.type = type;
        switch (type) {
            case INHERITED: {
                super.out = base;
                break;
            }
            case FORMATTED: {
//                    if(ansi==null){
//                        ansi= CoreIOUtils.convertOutputStream(base,NutsTerminalMode.FORMATTED,null);
//                    }
                super.out = formatted;
                break;
            }
            case FILTERED: {
//                    if(ansi==null){
//                        ansi= CoreIOUtils.convertOutputStream(base,NutsTerminalMode.FILTERED,null);
//                    }
                super.out = baseStripped;
                break;
            }
        }
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        switch (type) {
            case FORMATTED:
                return NutsTerminalModeOp.FORMAT;
            case FILTERED:
                return NutsTerminalModeOp.FILTER;
            case INHERITED:
                return NutsTerminalModeOp.NOP;
            default: {
                throw new IllegalArgumentException("Unsupported " + type);
            }
        }
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                return new NutsSystemOutputStream(base, NutsTerminalMode.INHERITED, session);
            }
            case FORMAT: {
                return new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED, session);
            }
            case FILTER: {
                return new NutsSystemOutputStream(base, NutsTerminalMode.FILTERED, session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED, session), session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED, session), session);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public String toString() {
        return "NutsSystemOutputStream(" + type + ')';
    }

    private class AnsiResetOnCloseOutputStream extends BaseTransparentFilterOutputStream {

        public AnsiResetOnCloseOutputStream(OutputStream base) {
            super(base);
        }

        @Override
        public void close() throws IOException {
            write("\033[0m".getBytes());
            flush();
            super.close();
        }
    }
}
