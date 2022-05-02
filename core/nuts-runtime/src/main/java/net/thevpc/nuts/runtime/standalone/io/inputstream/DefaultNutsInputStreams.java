package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.io.NutsInputStreams;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.io.util.NullInputStream;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.InputStream;

public class DefaultNutsInputStreams implements NutsInputStreams {
    private final NutsSession session;

    public DefaultNutsInputStreams(NutsSession session) {
        this.session = session;
    }

    @Override
    public InputStream ofNull() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public boolean isStdin(InputStream in) {
        return in == stdin();
    }

    @Override
    public InputStream stdin() {
        return getBootModel().getSystemTerminal().in();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    private DefaultNutsBootModel getBootModel() {
        return NutsWorkspaceExt.of(session).getModel().bootModel;
    }
}
