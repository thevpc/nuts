package net.vpc.app.nuts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class NutsNonFormattedPrintStream extends NutsPrintStream{
    public NutsNonFormattedPrintStream() {
    }

    public NutsNonFormattedPrintStream(OutputStream out) {
        super(out);
    }

    public NutsNonFormattedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public NutsNonFormattedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public NutsNonFormattedPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public NutsNonFormattedPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public NutsNonFormattedPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public NutsNonFormattedPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

}
