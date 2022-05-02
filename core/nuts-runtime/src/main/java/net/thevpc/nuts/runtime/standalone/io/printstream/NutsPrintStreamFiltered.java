package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextStyled;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;

public class NutsPrintStreamFiltered extends NutsPrintStreamRendered {
    public NutsPrintStreamFiltered(NutsPrintStreamBase base, NutsSession session, Bindings bindings) {
        super(base, session, NutsTerminalMode.FILTERED,
                bindings);
        setFormattedName(new DefaultNutsTextStyled(session,new DefaultNutsTextPlain(session,"<filtered-stream>" ), NutsTextStyles.of(NutsTextStyle.path())));
        if (bindings.filtered != null) {
            throw new IllegalArgumentException("already bound ansi");
        }
        bindings.filtered = this;
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsPrintStreamFiltered(base,session,new Bindings());
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NutsPrintStreamFormatted(base, getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        //do nothing!!
        return this;
    }
}
