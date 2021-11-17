package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;
import net.thevpc.nuts.runtime.standalone.text.FormattedPrintStreamRenderer;

public abstract class NutsPrintStreamRendered extends NutsPrintStreamBase {
    protected FormatOutputStreamSupport support;
    protected NutsPrintStreamBase base;
    protected FormattedPrintStreamRenderer renderer;

    public NutsPrintStreamRendered(NutsPrintStreamBase base, NutsSession session, NutsTerminalMode mode, FormattedPrintStreamRenderer renderer, Bindings bindings) {
        super(true, mode, session, bindings);
        this.base=base;
        this.support =new FormatOutputStreamSupport(new NutsPrintStreamHelper(base),renderer,session);
    }

    public NutsPrintStreamBase getBase() {
        return base;
    }

    @Override
    public NutsPrintStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NutsPrintStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf, int off, int len) {
        support.processChars(buf, 0, buf.length);
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        if (s == null) {
            write("null".toCharArray());
        } else {
            write(s.toCharArray());
        }
        return this;
    }

    @Override
    public int getColumns() {
        return setMode(NutsTerminalMode.INHERITED).getColumns();
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NutsPrintStreamFiltered(base, getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

}
