package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceAware;
import net.thevpc.nuts.runtime.format.text.util.FormattedPrintStreamUtils;
import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.*;
import java.util.Locale;

public class PrintStreamExt extends PrintStream implements ExtendedFormatAware, NutsWorkspaceAware {
    private OutputStream out;
    private boolean autoFlash2;
    private NutsWorkspace ws;

    public PrintStreamExt(OutputStream out) {
        super(out);
        this.out = out;
        this.autoFlash2 = false;
    }

    public PrintStreamExt(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        this.out = out;
        this.autoFlash2 = autoFlush;
    }

    public PrintStreamExt(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.out = out;
        this.autoFlash2 = autoFlush;
    }

    public PrintStreamExt(String fileName) throws FileNotFoundException {
        this(new FileOutputStream(fileName), false);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(fileName), false, csn);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(File file) throws FileNotFoundException {
        this(new FileOutputStream(file), false);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(file), false, csn);
        this.autoFlash2 = false;
    }

    public boolean isAutoFlash() {
        return autoFlash2;
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        OutputStream base = getOut();
        if (base instanceof ExtendedFormatAware) {
            return ((ExtendedFormatAware) base).getModeOp();
        }
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        if(out instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) out).convert(other);
        }
        return new RawOutputStream(out,ws).convert(other);
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws=workspace;
    }

    @Override
    public PrintStreamExt format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws,l, format, args));
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args));
        return this;
    }

}
