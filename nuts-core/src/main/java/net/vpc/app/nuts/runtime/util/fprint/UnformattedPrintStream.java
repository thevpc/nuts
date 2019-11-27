package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceAware;
import net.vpc.app.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;

import java.io.*;
import java.util.Locale;

public class UnformattedPrintStream extends PrintStream implements NutsWorkspaceAware {

    private NutsWorkspace ws;
    private FormattedPrintStream fout;

    public UnformattedPrintStream(FormattedPrintStream out) {
        super(out);
        this.fout = fout;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws=workspace;
    }

    @Override
    public UnformattedPrintStream format(Locale l, String format, Object... args) {
        boolean b = fout.isFormatEnabled();
        fout.setFormatEnabled(false);
        try {
            fout.format(l, format, args);
        } finally {
            if (b) {
                fout.setFormatEnabled(true);
            }
        }
        return this;
    }

    public PrintStream format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args));
        return this;
    }

    @Override
    public void println(String text) {
        boolean b = fout.isFormatEnabled();
        fout.setFormatEnabled(false);
        try {
            fout.println(text);
        } finally {
            if (b) {
                fout.setFormatEnabled(true);
            }
        }
    }

    @Override
    public void print(String text) {
        boolean b = fout.isFormatEnabled();
        fout.setFormatEnabled(false);
        try {
            fout.print(text);
        } finally {
            if (b) {
                fout.setFormatEnabled(true);
            }
        }
    }
}
