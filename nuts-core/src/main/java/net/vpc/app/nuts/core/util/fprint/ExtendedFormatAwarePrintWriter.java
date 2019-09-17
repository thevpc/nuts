package net.vpc.app.nuts.core.util.fprint;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceAware;
import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;

import java.io.*;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware, NutsWorkspaceAware {
    private NutsWorkspace ws;

    public ExtendedFormatAwarePrintWriter(Writer out) {
        super(out);
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out) {
        super(out);
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ExtendedFormatAwarePrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public ExtendedFormatAwarePrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public ExtendedFormatAwarePrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public ExtendedFormatAwarePrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public ExtendedFormatAwarePrintWriter format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws, l, format, args));
        return this;
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws, Locale.getDefault(), format, args));
        return this;
    }

    @Override
    public void flush() {
        super.flush();
    }

}
