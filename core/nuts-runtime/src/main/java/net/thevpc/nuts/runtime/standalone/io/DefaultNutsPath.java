package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.io.PrintStream;
import java.util.Objects;

public class DefaultNutsPath implements NutsPath {
    private String value;
    private NutsSession session;

    public DefaultNutsPath(String value,NutsSession session) {
        if(value==null){
            throw new IllegalArgumentException("invalid path");
        }
        if(session==null){
            throw new IllegalArgumentException("invalid session");
        }
        this.value = value;
        //session will be used later
        this.session = session;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsPath that = (DefaultNutsPath) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public NutsFormat formatter() {
        return new PathFormat(this);
    }

    private static class PathFormat extends DefaultFormatBase<NutsFormat> {
        private DefaultNutsPath p;

        public PathFormat(DefaultNutsPath p) {
            super(p.session.getWorkspace(), "path");
            this.p=p;
        }

        @Override
        public void print(PrintStream out) {
            out.print(p.session.getWorkspace().text().forStyled(p.value, NutsTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }
}
