package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;

import java.io.PrintStream;

public class PrintStreamFromNutsPrintStream extends PrintStream {
    private NutsPrintStream base;

    public PrintStreamFromNutsPrintStream(NutsPrintStream out) {
        super(new OutputStreamFromNutsPrintStream(out));
        this.base = base;
    }

    public NutsPrintStream getBase() {
        return base;
    }
}
