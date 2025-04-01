package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFormatted extends NPrintStreamRendered {
    public NPrintStreamFormatted(NPrintStreamBase base, Bindings bindings) {
        super(base, NTerminalMode.FORMATTED, bindings);
        getMetaData().setMessage(NMsg.ofStyledPath("<formatted-stream>"));
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

    @Override
    public NPrintStream run(NTerminalCmd command) {
        flush();
        print(NText.ofCommand(command));
        flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
