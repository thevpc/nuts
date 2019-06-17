package net.vpc.app.nuts.core.util.fprint;

import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;

import java.io.*;
import java.util.Locale;

public class UnformattedPrintStream extends PrintStream {

    private FormattedPrintStream fout;

    public UnformattedPrintStream(FormattedPrintStream out) {
        super(out);
        this.fout = fout;
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
        print(FormattedPrintStreamUtils.format(Locale.getDefault(), format, args));
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
