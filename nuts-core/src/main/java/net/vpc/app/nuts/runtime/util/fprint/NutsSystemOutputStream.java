package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.io.NutsTerminalModeOp;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiPrintStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NutsSystemOutputStream extends FilterOutputStream implements ExtendedFormatAware {

    private NutsTerminalMode type;
    private OutputStream base;
    private OutputStream baseStripped;
    private OutputStream ansi;
    private NutsWorkspace ws;

    public NutsSystemOutputStream(OutputStream base, NutsTerminalMode type, NutsWorkspace ws) {
        super(base);
        this.ws = ws;
        this.type = type;
        this.base = base;
        this.baseStripped =CoreIOUtils.convertOutputStream(base,NutsTerminalMode.FILTERED, ws);
        if (AnsiPrintStreamSupport.IS_WINDOWS && !AnsiPrintStreamSupport.IS_CYGWIN && !AnsiPrintStreamSupport.IS_MINGW_XTERM) {
            // On windows we know the console does not interpret ANSI codes..
            try {
                this.ansi = CoreIOUtils.convertOutputStream(new WindowsAnsiPrintStream((base instanceof PrintStream)?((PrintStream) base):new PrintStream(base)),NutsTerminalMode.FORMATTED,ws);
            } catch (Throwable ignore) {
                this.ansi = CoreIOUtils.convertOutputStream(new AnsiOutputStream(base),NutsTerminalMode.FORMATTED,ws);
            }
        } else {
            FilterOutputStream filterOutputStream = new FilterOutputStream(base) {
                @Override
                public void close() throws IOException {
                    write(AnsiOutputStream.RESET_CODE);
                    flush();
                    super.close();
                }
            };
            ansi = CoreIOUtils.convertOutputStream(filterOutputStream,NutsTerminalMode.FORMATTED,ws);
        }
        setType(type);
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
                super.out = ansi;
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
        switch (other){
            case NOP:{
                return new NutsSystemOutputStream(base, NutsTerminalMode.INHERITED,ws);
            }
            case FORMAT:{
                return new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED,ws);
            }
            case FILTER:{
                return new NutsSystemOutputStream(base, NutsTerminalMode.FILTERED,ws);
            }
            case ESCAPE:{
                return new EscapeOutputStream(new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED,ws));
            }
            case UNESCAPE:{
                return new UnescapeOutputStream(new NutsSystemOutputStream(base, NutsTerminalMode.FORMATTED,ws));
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

}
