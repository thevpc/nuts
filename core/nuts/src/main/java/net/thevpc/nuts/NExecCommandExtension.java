package net.thevpc.nuts;

import net.thevpc.nuts.spi.NComponent;

public interface NExecCommandExtension extends NComponent {
    int exec(NExecCommandExtensionContext context);
}
