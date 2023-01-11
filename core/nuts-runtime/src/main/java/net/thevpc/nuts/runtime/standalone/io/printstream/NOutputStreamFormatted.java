package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.OutputStream;

public class NOutputStreamFormatted extends NOutputStreamRendered {
    public NOutputStreamFormatted(NOutputStreamBase base, NSession session, Bindings bindings) {
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
    public NOutputStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NOutputStreamFormatted(base, session, new Bindings());
    }

    @Override
    protected NOutputStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NOutputStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(base.getSession(), NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NOutputStream run(NTerminalCommand command, NSession session) {
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
