package net.thevpc.nuts.platform;

import net.thevpc.nuts.net.NConnectionString;

public interface NEnvCmdSPI {
    String exec(String cmd);
    NConnectionString getTargetConnectionString();
}
