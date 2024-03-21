package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFiltered extends NPrintStreamRendered {
    public NPrintStreamFiltered(NPrintStreamBase base, NSession session, Bindings bindings) {
        super(base, session, NTerminalMode.FILTERED,
                bindings);
        getMetaData().setMessage(NMsg.ofStyled( "<filtered-stream>", NTextStyle.path()));
    }

    @Override
    public NPrintStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NPrintStreamFiltered(base, session, new Bindings());
    }

    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NPrintStreamFormatted(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NPrintStream run(NTerminalCommand command, NSession session) {
        //do nothing!!
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
