package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.OutputStream;

public class NStreamFiltered extends NStreamRendered {
    public NStreamFiltered(NStreamBase base, NSession session, Bindings bindings) {
        super(base, session, NTerminalMode.FILTERED,
                bindings);
        getOutputMetaData().setMessage(
                NMsg.ofStyled( "<filtered-stream>", NTextStyle.path())
                );
        if (bindings.filtered != null) {
            throw new IllegalArgumentException("already bound ansi");
        }
        bindings.filtered = this;
    }

    @Override
    public NStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NStreamFiltered(base, session, new Bindings());
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NStreamFormatted(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
        //do nothing!!
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
