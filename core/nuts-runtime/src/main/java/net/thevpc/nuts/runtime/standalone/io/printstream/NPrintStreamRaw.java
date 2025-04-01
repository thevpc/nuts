package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Print stream from custom output streams like ByteArrayOutputStream
 */
public class NPrintStreamRaw extends NPrintStreamBase {
    protected OutputStream out;
    private PrintStream base;

    protected NPrintStreamRaw(OutputStream out, PrintStream base, Boolean autoFlush, NTerminalMode mode, NWorkspace workspace, Bindings bindings, NSystemTerminalBase term) {
        super(autoFlush == null || autoFlush, mode, bindings, term);
        getMetaData().setMessage(NMsg.ofNtf(NText.ofStyledPath("<raw-stream>")));
        this.out = out;
        this.base = base;
    }

    public NPrintStreamRaw(OutputStream out, Boolean autoFlush, String encoding, Bindings bindings, NSystemTerminalBase term) {
        this(out, null, autoFlush, encoding, bindings, term);
    }

    public NPrintStreamRaw(OutputStream out, NTerminalMode mode, Boolean autoFlush,
                           String encoding,
                           Bindings bindings,
                           NSystemTerminalBase term) {
        super(true, mode == null ? NTerminalMode.INHERITED : mode, bindings, term);
        getMetaData().setMessage(NMsg.ofNtf(NText.ofStyledPath("<raw-stream>")));
        this.out = out;
        if (out instanceof PrintStream) {
            PrintStream ps = (PrintStream) out;
            if (autoFlush == null && encoding == null) {
                base = ps;
            }
        }
        if (base == null) {
            try {
                this.base =
                        encoding == null ?
                                new PrintStream(out, autoFlush != null && autoFlush)
                                : new PrintStream(out, autoFlush != null && autoFlush, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
//        switch (getTerminalMode()) {
//            case ANSI: {
//                if (bindings.ansi != null) {
//                    throw new IllegalArgumentException("already bound ansi");
//                }
//                bindings.ansi = this;
//                if (bindings.inherited == null) {
//                    bindings.inherited = this;
//                }
//                if (bindings.raw == null) {
//                    bindings.raw = this;
//                }
//                break;
//            }
//            case INHERITED: {
//                if (bindings.inherited != null) {
//                    throw new IllegalArgumentException("already bound ansi");
//                }
//                bindings.inherited = this;
//                break;
//            }
//        }
    }

    public PrintStream getBase() {
        return base;
    }

    @Override
    public NPrintStream flush() {
        base.flush();
        return this;
    }

    @Override
    public NPrintStream close() {
        if (getTerminalMode() == NTerminalMode.ANSI) {
            print("\033[0m".getBytes());
            flush();
        }
        base.close();
        return this;
    }

    @Override
    public NPrintStream write(int b) {
        base.write(b);
        return this;
    }

    @Override
    public NPrintStream write(byte[] buf, int off, int len) {
        if (buf == null) {
            base.print("null");
        } else {
            base.write(buf, off, len);
        }
        return this;
    }

    @Override
    public NPrintStream write(char[] s, int off, int len) {
        if (s == null) {
            base.print("null");
        } else {
            base.print(new String(s, off, len));
        }
        return this;
    }

    @Override
    public NPrintStream run(NTerminalCmd command) {
        return this;
    }

    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NPrintStreamFormatted(this, bindings);
            }
            case FILTERED: {
                return new NPrintStreamFiltered(this, bindings);
            }
            case ANSI: {
                if(this.getTerminalMode()==NTerminalMode.INHERITED){
                    return this;
                }
                break;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        if (buf == null) {
            base.print("null");
        } else {
            base.write(buf, off, len);
        }
        return this;
    }
}
