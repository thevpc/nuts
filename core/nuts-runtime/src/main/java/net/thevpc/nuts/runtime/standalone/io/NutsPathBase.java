package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.util.Objects;

public abstract class NutsPathBase implements NutsPath {
    private NutsSession session;

    public NutsPathBase(NutsSession session) {
        if(session==null){
            throw new IllegalArgumentException("invalid session");
        }
        //session will be used later
        this.session = session;
    }

    protected NutsSession getSession() {
        return session;
    }

    @Override
    public NutsFormat formatter() {
        return new PathFormat(this)
                .setSession(session)
                ;
    }

    public NutsString toNutsString(){
        return session.getWorkspace().text().forPlain(toString());
    }

    private static class PathFormat extends DefaultFormatBase<NutsFormat> {
        private NutsPathBase p;

        public PathFormat(NutsPathBase p) {
            super(p.session.getWorkspace(), "path");
            this.p=p;
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(p.session.getWorkspace().text().forStyled(p.toNutsString(), NutsTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }
}
