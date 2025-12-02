package net.thevpc.nuts.spi;


import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecTargetInfo;
import net.thevpc.nuts.net.NConnectionString;

public interface NExecTargetInfoContext {
    NConnectionString getConnectionString();

    NExecCmd getExecCommand();

    NExecTargetInfo createDefaultTargetInfo(NExecTargetInfoRunner runner);
}
