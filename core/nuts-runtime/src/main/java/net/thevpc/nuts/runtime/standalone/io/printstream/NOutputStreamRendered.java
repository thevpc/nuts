package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;

public abstract class NOutputStreamRendered extends NOutputStreamBase {
    protected FormatOutputStreamSupport support;
    protected NOutputStreamBase base;

    public NOutputStreamRendered(NOutputStreamBase base, NSession session, NTerminalMode mode, Bindings bindings) {
        super(true, mode, session, bindings, base.getTerminal());
        this.base = base;
        this.support = new FormatOutputStreamSupport(new NPrintStreamHelper(base), session, base.getTerminal(),
                (mode != NTerminalMode.ANSI && mode != NTerminalMode.FORMATTED)
        );
    }

    public NOutputStreamBase getBase() {
        return base;
    }

    @Override
    public NOutputStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NOutputStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NOutputStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NOutputStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NOutputStream write(char[] buf, int off, int len) {
        support.processChars(buf, off, len);
        return this;
    }


    @Override
    protected NOutputStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NOutputStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

}
