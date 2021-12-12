package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextStyled;

public class NutsPrintStreamNull extends NutsPrintStreamBase {
    public NutsPrintStreamNull(NutsSession session) {
        super(false, NutsTerminalMode.INHERITED, session, new Bindings(),null);
        setFormattedName(new DefaultNutsTextStyled(session,new DefaultNutsTextPlain(session,"<null-stream>" ),NutsTextStyles.of(NutsTextStyle.path())));
    }
    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsPrintStreamNull(session);
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        return this;
    }

    @Override
    public NutsPrintStream flush() {
        return this;
    }

    @Override
    public NutsPrintStream close() {
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        return this;
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        return this;
    }
}
