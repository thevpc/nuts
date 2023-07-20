package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSupported;
import net.thevpc.nuts.NWorkspaceExtension;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.spi.NExtensionLifeCycle;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class NJLineExtensionLifeCycle implements NExtensionLifeCycle {
    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NSupported.DEFAULT_SUPPORT;
    }

    @Override
    public void onInitExtension(NWorkspaceExtension extension, NSession session) {
        NIO.of(session).setSystemTerminal(new NJLineTerminal());
    }

    @Override
    public void onDisableExtension(NWorkspaceExtension extension, NSession session) {

    }

    @Override
    public void onEnableExtension(NWorkspaceExtension extension, NSession session) {

    }

    @Override
    public void onDestroyExtension(NWorkspaceExtension extension, NSession session) {
        NIO.of(session).setSystemTerminal(null);
    }
}
