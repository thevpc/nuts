package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;

public abstract class NStreamRendered extends NStreamBase {
    protected FormatOutputStreamSupport support;
    protected NStreamBase base;
    public NStreamRendered(NStreamBase base, NSession session, NTerminalMode mode, Bindings bindings) {
        super(true, mode, session, bindings,base.getTerminal());
        this.base=base;
        this.support =new FormatOutputStreamSupport(new NPrintStreamHelper(base),session,base.getTerminal(),
                (mode!= NTerminalMode.ANSI && mode!= NTerminalMode.FORMATTED)
                );
    }

    public NStreamBase getBase() {
        return base;
    }

    @Override
    public NStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NStream write(char[] buf, int off, int len) {
        support.processChars(buf, 0, buf.length);
        return this;
    }

    @Override
    public NStream print(String s) {
        if (s == null) {
            write("null".toCharArray());
        } else {
            write(s.toCharArray());
        }
        return this;
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NStreamFiltered(base, getSession(),bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

}
