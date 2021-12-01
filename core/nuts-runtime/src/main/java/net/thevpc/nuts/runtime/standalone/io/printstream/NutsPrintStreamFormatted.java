package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;

public class NutsPrintStreamFormatted extends NutsPrintStreamRendered {
    public NutsPrintStreamFormatted(NutsPrintStreamBase base, NutsSession session, Bindings bindings) {
        super(base,session,NutsTerminalMode.FORMATTED,
                bindings);
        if(bindings.formatted!=null){
            throw new NutsIllegalArgumentException(session,NutsMessage.plain("formatted already bound"));
        }
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
