package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NWorkspaceExtension;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.spi.NExtensionLifeCycle;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NUnused;

@NUnused
public class NJLineExtensionLifeCycle implements NExtensionLifeCycle {
    public NJLineExtensionLifeCycle() {
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public void onInitExtension(NWorkspaceExtension extension) {
        NIO.of().setSystemTerminal(new NJLineTerminal());
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
