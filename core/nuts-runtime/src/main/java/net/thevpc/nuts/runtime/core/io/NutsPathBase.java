package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.net.URL;
import java.nio.file.Path;

public abstract class NutsPathBase implements NutsPath {
    private NutsSession session;

    public NutsPathBase(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("invalid session");
        }
        //session will be used later
        this.session = session;
    }
    @Override
    public NutsString getFormattedName() {
        return getSession().getWorkspace().text().forStyled(getName(),NutsTextStyle.path());
    }

    @Override
    public boolean isURL() {
        return asURL()!=null;
    }

    @Override
    public boolean isFilePath() {
        return asFilePath()!=null;
    }

    public NutsSession getSession() {
        return session;
    }

    public URL asURL() {
        try {
            return toURL();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Path asFilePath() {
        try {
            return toFilePath();
        } catch (Exception ex) {
            return null;
        }
    }


    public NutsString toNutsString() {
        return session.getWorkspace().text().forPlain(toString());
    }

    @Override
    public NutsFormat formatter() {
        return new PathFormat(this)
                .setSession(getSession())
                ;
    }

    private static class PathFormat extends DefaultFormatBase<NutsFormat> {
        private NutsPathBase p;

        public PathFormat(NutsPathBase p) {
            super(p.session.getWorkspace(), "path");
            this.p = p;
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
