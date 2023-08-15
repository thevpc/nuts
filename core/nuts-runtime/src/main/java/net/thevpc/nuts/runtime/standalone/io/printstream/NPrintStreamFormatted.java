package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFormatted extends NPrintStreamRendered {
    public NPrintStreamFormatted(NPrintStreamBase base, NSession session, Bindings bindings) {
        super(base, session, NTerminalMode.FORMATTED,
                bindings);
        if (bindings.formatted != null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("formatted already bound"));
        }
        getMetaData().setMessage(
                NMsg.ofStyled(
                        "<formatted-stream>", NTextStyle.path())
        );
        bindings.formatted = this;
    }

    @Override
    public NPrintStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NPrintStreamFormatted(base, session, new Bindings());
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

    @Override
    public NPrintStream run(NTerminalCommand command, NSession session) {
        flush();
        print(NTexts.of(this.session).ofCommand(command));
        flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
