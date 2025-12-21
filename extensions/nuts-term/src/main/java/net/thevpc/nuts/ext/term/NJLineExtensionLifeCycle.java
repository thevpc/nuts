package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.core.NWorkspaceExtension;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.spi.NExtensionLifeCycle;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NUnused;

@NUnused
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NJLineExtensionLifeCycle implements NExtensionLifeCycle {
    public NJLineExtensionLifeCycle() {
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
