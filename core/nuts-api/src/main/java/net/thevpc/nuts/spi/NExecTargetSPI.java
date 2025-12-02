package net.thevpc.nuts.spi;

import net.thevpc.nuts.command.NExecTargetInfo;

public interface NExecTargetSPI extends NComponent {
    int exec(NExecTargetCommandContext context);

    NExecTargetInfo getTargetInfo(NExecTargetInfoContext context);
}
