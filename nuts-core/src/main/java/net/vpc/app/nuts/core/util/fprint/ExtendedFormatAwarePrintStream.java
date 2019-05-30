package net.vpc.app.nuts.core.util.fprint;

import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;

import java.io.*;
import java.util.Locale;

public class ExtendedFormatAwarePrintStream extends PrintStream implements ExtendedFormatAware {

    public ExtendedFormatAwarePrintStream(OutputStream out) {
        super(out);
    }

    public ExtendedFormatAwarePrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ExtendedFormatAwarePrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public ExtendedFormatAwarePrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public ExtendedFormatAwarePrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public ExtendedFormatAwarePrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public ExtendedFormatAwarePrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public ExtendedFormatAwarePrintStream format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.format(l, format, args));
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        print(FormattedPrintStreamUtils.format(Locale.getDefault(), format, args));
        return this;
    }

}
