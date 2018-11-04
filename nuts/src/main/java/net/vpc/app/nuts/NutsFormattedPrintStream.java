package net.vpc.app.nuts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public abstract class NutsFormattedPrintStream extends NutsPrintStream{
    public NutsFormattedPrintStream() {
    }

    public NutsFormattedPrintStream(OutputStream out) {
        super(out);
    }

    public NutsFormattedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public NutsFormattedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public NutsFormattedPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public NutsFormattedPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public NutsFormattedPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public NutsFormattedPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

}
