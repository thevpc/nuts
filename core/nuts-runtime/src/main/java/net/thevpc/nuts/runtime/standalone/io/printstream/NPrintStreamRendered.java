package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyled;

public abstract class NPrintStreamRendered extends NPrintStreamBase {
    protected FormatOutputStreamSupport support;
    protected NPrintStreamBase base;

    public NPrintStreamRendered(NPrintStreamBase base, NSession session, NTerminalMode mode, Bindings bindings) {
        super(true, mode, session, bindings, base.getTerminal());
        this.base = base;
        this.support = new FormatOutputStreamSupport(base, session, base.getTerminal(),
                (mode != NTerminalMode.ANSI && mode != NTerminalMode.FORMATTED)
        );
    }

    public NPrintStreamBase getBase() {
        return base;
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        support.writeRaw(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NPrintStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NPrintStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NPrintStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream write(char[] buf, int off, int len) {
        support.processChars(buf, off, len);
        return this;
    }

    protected NPrintStream printParsed(NText b) {
        if (isNtf()) {
            support.pushNode(b);
        } else {
            switch (b.getType()) {
                case PLAIN: {
                    support.pushNode(b);
                    break;
                }
                case COMMAND: {
                    //ignore
                    break;
                }
                case STYLED: {
                    printParsed(((NTextStyled) b).getChild());
                    break;
                }
                default: {
                    throw new IllegalArgumentException("not supported");
                }
            }
        }
        return this;
    }

    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NPrintStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

}
