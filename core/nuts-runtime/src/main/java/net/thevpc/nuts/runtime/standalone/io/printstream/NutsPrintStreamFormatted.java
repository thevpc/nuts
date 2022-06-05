package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.io.OutputStream;

public class NutsPrintStreamFormatted extends NutsPrintStreamRendered {
    public NutsPrintStreamFormatted(NutsPrintStreamBase base, NutsSession session, Bindings bindings) {
        super(base, session, NutsTerminalMode.FORMATTED,
                bindings);
        if (bindings.formatted != null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("formatted already bound"));
        }
        getOutputMetaData().setMessage(
                NutsMessage.ofStyled(
                        "<formatted-stream>", NutsTextStyle.path())
        );
        bindings.formatted = this;
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NutsPrintStreamFormatted(base, session, new Bindings());
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NutsPrintStreamFiltered(base, getSession(), bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(), NutsMessage.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        flush();
        printf("%s", NutsTexts.of(this.session).ofCommand(command));
        flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
