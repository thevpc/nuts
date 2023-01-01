package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.OutputStream;

public class NOutStreamFormatted extends NOutStreamRendered {
    public NOutStreamFormatted(NOutStreamBase base, NSession session, Bindings bindings) {
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
    public NOutStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NOutStreamFormatted(base, session, new Bindings());
    }

    @Override
    protected NOutStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NOutStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NOutStream run(NTerminalCommand command, NSession session) {
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
