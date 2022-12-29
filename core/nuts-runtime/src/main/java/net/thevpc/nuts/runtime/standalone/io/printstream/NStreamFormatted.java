package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.OutputStream;

public class NStreamFormatted extends NStreamRendered {
    public NStreamFormatted(NStreamBase base, NSession session, Bindings bindings) {
        super(base, session, NTerminalMode.FORMATTED,
                bindings);
        if (bindings.formatted != null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("formatted already bound"));
        }
        getOutputMetaData().setMessage(
                NMsg.ofStyled(
                        "<formatted-stream>", NTextStyle.path())
        );
        bindings.formatted = this;
    }

    @Override
    public NStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NStreamFormatted(base, session, new Bindings());
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
        flush();
        printf("%s", NTexts.of(this.session).ofCommand(command));
        flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
