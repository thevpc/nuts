package net.thevpc.nuts.runtime.standalone.io.outputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.text.EscapeOutputStream;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;
import net.thevpc.nuts.runtime.standalone.text.UnescapeOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.terminals.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
//import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;

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
        /*if (ws.env().getOptionAsBoolean("enableJansi",false) && OptionalJansi.isAvailable()) {
            OutputStream f = OptionalJansi.preparestream(base);
            if(f!=null){
                this.formatted = CoreIOUtils.convertOutputStream(base, NutsTerminalMode.FORMATTED, session);
                setType(type);
            }else{
                this.formatted = baseStripped;
                setType(NutsTerminalMode.FILTERED);
            }
        }else*/
        {
            NutsOsFamily os = session.env().getOsFamily();
            if ((os == NutsOsFamily.WINDOWS && (CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM))
                    || os == NutsOsFamily.LINUX || os == NutsOsFamily.UNIX || os == NutsOsFamily.MACOS) {
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
        }
        throw new NutsUnsupportedEnumException(session, type);
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
        throw new NutsUnsupportedEnumException(session, other);
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
