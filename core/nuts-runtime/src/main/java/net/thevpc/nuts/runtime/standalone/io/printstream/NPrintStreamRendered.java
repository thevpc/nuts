package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyled;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

public abstract class NPrintStreamRendered extends NPrintStreamBase {
    protected FormatOutputStreamSupport support;
    protected NPrintStreamBase base;
    protected boolean lastWasProgress=false;

    public NPrintStreamRendered(NPrintStreamBase base, NTerminalMode mode, Bindings bindings) {
        super(true, mode, bindings, base.getTerminal());
        this.base = base;
        this.support = new FormatOutputStreamSupport(base, base.getTerminal(),
                (mode != NTerminalMode.ANSI && mode != NTerminalMode.FORMATTED)
        );
    }

    public void flushTransientLine(){
        if(lastWasProgress){
            support.pushNode(NText.ofCommand(NTerminalCmd.CLEAR_LINE));
            support.pushNode(NText.ofCommand(NTerminalCmd.MOVE_LINE_START));
            support.flush();
            lastWasProgress=false;
        }
    }

    public NPrintStreamBase getBase() {
        return base;
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        flushTransientLine();
        support.writeRaw(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream flush() {
        flushTransientLine();
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public void close() {
        flush();
        flushTransientLine();
        base.close();
    }

    @Override
    public NPrintStream write(int b) {
        flushTransientLine();
        support.processByte(b);
        return this;
    }

    @Override
    public NPrintStream write(byte[] buf, int off, int len) {
        flushTransientLine();
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream write(char[] buf, int off, int len) {
        flushTransientLine();
        support.processChars(buf, off, len);
        return this;
    }

    @Override
    public NPrintStream printProgressLine(NText b) {
        lastWasProgress = true;
        for (NText line : b.split("\n\r")) {
            support.pushNode(NText.ofCommand(NTerminalCmd.CLEAR_LINE));
            support.pushNode(NText.ofCommand(NTerminalCmd.MOVE_LINE_START));
            support.flush();
            if (isNtf()) {
                support.pushNode(line);
            } else {
                switch (line.type()) {
                    case PLAIN: {
                        support.pushNode(line);
                        break;
                    }
                    case COMMAND: {
                        //ignore
                        break;
                    }
                    case STYLED: {
                        printParsed(((NTextStyled) line).getChild());
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("not supported");
                    }
                }
            }
            // after first line always exit
            break;
        }
        return this;
    }

    protected NPrintStream printParsed(NText b) {
        flushTransientLine();
        if (isNtf()) {
            support.pushNode(b);
        } else {
            switch (b.type()) {
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
                return new NPrintStreamFiltered(base, bindings);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

}
