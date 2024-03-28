package net.thevpc.nuts;

import net.thevpc.nuts.spi.NComponent;

public interface NExecCmdExtension extends NComponent {
    int exec(NExecCmdExtensionContext context);
}
