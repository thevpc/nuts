package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextStyled;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.text.NutsTexts;

public class NutsPrintStreamFormatted extends NutsPrintStreamRendered {
    public NutsPrintStreamFormatted(NutsPrintStreamBase base, NutsSession session, Bindings bindings) {
        super(base,session, NutsTerminalMode.FORMATTED,
                bindings);
        if(bindings.formatted!=null){
            throw new NutsIllegalArgumentException(session,NutsMessage.plain("formatted already bound"));
        }
        setFormattedName(new DefaultNutsTextStyled(session,new DefaultNutsTextPlain(session,"<formatted-stream>" ), NutsTextStyles.of(NutsTextStyle.path())));
        bindings.formatted=this;
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsPrintStreamFormatted(base,session,new Bindings());
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other){
            case FILTERED:{
                return new NutsPrintStreamFiltered(base,getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        flush();
        printf("%s", NutsTexts.of(this.session).ofCommand(command));
        flush();
        return this;
    }

}
