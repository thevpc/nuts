package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsInputStreams;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.io.NullInputStream;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
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
        return in == getModel().stdin();
    }

    @Override
    public InputStream stdin() {
        return getModel().stdin();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }

    private DefaultNutsWorkspaceConfigModel getModel() {
        return ((DefaultNutsWorkspaceConfigManager) session.config()).getModel();
    }
}
