package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;

public abstract class NOutStreamRendered extends NOutStreamBase {
    protected FormatOutputStreamSupport support;
    protected NOutStreamBase base;
    public NOutStreamRendered(NOutStreamBase base, NSession session, NTerminalMode mode, Bindings bindings) {
        super(true, mode, session, bindings,base.getTerminal());
        this.base=base;
        this.support =new FormatOutputStreamSupport(new NPrintStreamHelper(base),session,base.getTerminal(),
                (mode!= NTerminalMode.ANSI && mode!= NTerminalMode.FORMATTED)
                );
    }

    public NOutStreamBase getBase() {
        return base;
    }

    @Override
    public NOutStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NOutStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NOutStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NOutStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NOutStream write(char[] buf, int off, int len) {
        support.processChars(buf, 0, buf.length);
        return this;
    }

    @Override
    public NOutStream print(String s) {
        if (s == null) {
            write("null".toCharArray());
        } else {
            write(s.toCharArray());
        }
        return this;
    }

    @Override
    protected NOutStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NOutStreamFiltered(base, getSession(),bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

}
