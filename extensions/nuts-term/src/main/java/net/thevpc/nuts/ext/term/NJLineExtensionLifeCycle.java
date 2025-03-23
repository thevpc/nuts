package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.NWorkspaceExtension;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.spi.NExtensionLifeCycle;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class NJLineExtensionLifeCycle implements NExtensionLifeCycle {
    private NWorkspace workspace;

    public NJLineExtensionLifeCycle(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public void onInitExtension(NWorkspaceExtension extension) {
        NIO.of().setSystemTerminal(new NJLineTerminal(workspace));
    }

    @Override
    public void onDisableExtension(NWorkspaceExtension extension) {

    }

    @Override
    public void onEnableExtension(NWorkspaceExtension extension) {

    }

    @Override
    public void onDestroyExtension(NWorkspaceExtension extension) {
        NIO.of().setSystemTerminal(null);
    }
}
