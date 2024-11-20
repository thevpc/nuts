package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFiltered extends NPrintStreamRendered {
    public NPrintStreamFiltered(NPrintStreamBase base, NWorkspace workspace, Bindings bindings) {
        super(base, workspace, NTerminalMode.FILTERED,
                bindings);
        getMetaData().setMessage(NMsg.ofStyled( "<filtered-stream>", NTextStyle.path()));
    }


    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NPrintStreamFormatted(base, workspace, bindings);
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
