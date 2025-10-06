package net.thevpc.nuts.command;

import net.thevpc.nuts.spi.NComponent;

public interface NExecCmdExtension extends NComponent {
    int exec(NExecCmdExtensionContext context);
}
