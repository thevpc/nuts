package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.runtime.core.format.text.FormatOutputStreamSupport;
import net.thevpc.nuts.runtime.core.format.text.FormattedPrintStreamRenderer;

public abstract class NutsPrintStreamRendered extends NutsPrintStreamBase {
    protected FormatOutputStreamSupport support;
    protected NutsPrintStreamBase base;
    protected FormattedPrintStreamRenderer renderer;

    public NutsPrintStreamRendered(NutsPrintStreamBase base, NutsTerminalMode mode, FormattedPrintStreamRenderer renderer, Bindings bindings) {
        super(true, mode, base.session, bindings);
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
        return convertMode(NutsTerminalMode.INHERITED).getColumns();
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NutsPrintStreamFiltered(base, bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

}
