package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFiltered extends NPrintStreamRendered {
    public NPrintStreamFiltered(NPrintStreamBase base, Bindings bindings) {
        super(base, NTerminalMode.FILTERED,
                bindings);
        getMetaData().setMessage(NMsg.ofStyledPath( "<filtered-stream>"));
    }


    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NPrintStreamFormatted(base, bindings);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NPrintStream run(NTerminalCmd command) {
        //do nothing!!
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
